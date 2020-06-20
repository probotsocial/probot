import postgrestClient from '@raphiniert/ra-data-postgrest';
import { fetchUtils } from 'ra-core';
const httpClient = fetchUtils.fetchJson
// const Client = require('node-rest-client').Client;
// const client = new Client ();

const postgrestDataProvider = postgrestClient('http://localhost:5000')

const hybridDataProvider = {
    ...postgrestDataProvider,
    updateMany: (resource, params) => {
        httpClient(`http://localhost:10000/probot/twitter/directmessage`, {
            method: 'POST',
            headers: { "Accept" : "application/json", "Content-Type": "application/json" },
            body: JSON.stringify(params),
        }).then(({json}) => ({data: json.map(data => data.id)}))
    }
}

export default hybridDataProvider;

