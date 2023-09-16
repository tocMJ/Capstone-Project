import subprocess
from fastapi import FastAPI
import time
import nacos
import threading

app = FastAPI()

SERVER_ADDRESSES = "127.0.0.1:8848"
NAMESPACE = "public"
DATA_ID = "config.properties"
GROUP = "DEFAULT_GROUP"
stop_event = threading.Event()
processes = []

successRate = None
requests = None
iterations = None
interval = None
config_lock = threading.Lock()
ab_lock = threading.Lock()

client = nacos.NacosClient(SERVER_ADDRESSES, namespace=NAMESPACE)


def update_config():
    global successRate, requests, iterations, interval
    with config_lock:
        config_content = client.get_config(DATA_ID, GROUP)
        settings = {}
        for line in config_content.strip().split('\n'):
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            key, value = line.split('=')
            settings[key] = value
        requests = int(settings.get('requests', requests))
        iterations = int(settings.get('iterations', iterations))
        interval = int(settings.get('service.selectionIntervalInSeconds', interval))


def poll_config():
    while not stop_event.is_set():
        update_config()
        time.sleep(interval)


def run_ab():
    cmd = ["ab", "-n", str(requests), "-c", "10", "http://localhost:8080/intelligentSelector?query=aapl"]
    process = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    processes.append(process)
    print(f"Total number of processes: {len(processes)}")
    stdout, stderr = process.communicate()
    return stdout, stderr



@app.get("/test")
def test():
    if requests is None or iterations is None:
        update_config()

    with ab_lock:
        results = []
        for _ in range(iterations):
            stdout, stderr = run_ab()
            results.append(stdout.decode('utf-8'))
            if _ < iterations - 1:
                time.sleep(interval)
        return {"results": results}


@app.on_event("startup")
async def startup_event():
    threading.Thread(target=poll_config).start()


@app.on_event("shutdown")
async def shutdown_event():
    stop_event.set()
    for process in processes:
        process.terminate()


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=2000)