import React, { useState } from 'react';
import axios from 'axios';

function IntelligentSelector() {
    const [query, setQuery] = useState('');
    const [data, setData] = useState(null);

    const handleInputChange = (event) => {
        setQuery(event.target.value);
    };

    const handleSearch = async () => {
        setData(null);
        try {
            const response = await axios.get(`http://localhost:8080/intelligentSelector`, {
                params: { query: query }
            });
            setData(response.data);
        } catch (error) {
            console.error('Error during search:', error);
            if (error.response) {
                if (error.response.status === 404) {
                    alert('Not connected. Please try again later.');
                } else if (error.response.status === 400) {
                    alert('Bad request. Please check your input and try again.');
                } else {
                    alert('An error occurred. Please try again later.');
                }
            } else {
                alert('An error occurred. Please try again later.');
            }
        }
    };

    const handleKeyDown = (event) => {
        if (event.key === 'Enter') {
            handleSearch();
        }
    };


    return (
        <div>
            <h1>Intelligent Selector</h1>
            <input
                type="text"
                value={query}
                onChange={handleInputChange}
                onKeyDown={handleKeyDown}
                placeholder="Enter news"
            />
            <button onClick={handleSearch}>Search News</button>
            {data && <pre>{JSON.stringify(data, null, 2)}</pre>}
        </div>
    );
}

export default IntelligentSelector;
