from flask import Flask, jsonify
import asyncio
import httpx

app = Flask(__name__)

async def send_request(url):
    async with httpx.AsyncClient() as client:
        resp = await client.get(url)
    return resp.status_code, resp.text

async def send_requests(url, n):
    tasks = [send_request(url) for _ in range(n)]
    responses = await asyncio.gather(*tasks)
    return responses

@app.route('/', methods=['GET'])
def home():
    return 'Hello'

@app.route('/test', methods=['GET'])
def test():
    url = "http://localhost:8080/randomSelector/aapl"
    n = 10  # 请求次数
    responses = asyncio.run(send_requests(url, n))
    return jsonify({'responses': responses})

if __name__ == "__main__":
    app.run(host="localhost", port=2000)
