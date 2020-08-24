import postgrestClient from '@raphiniert/ra-data-postgrest';
import { fetchUtils } from 'ra-core';
import { showNotification } from 'react-admin';
const httpClient = fetchUtils.fetchJson
// const Client = require('node-rest-client').Client;
// const client = new Client ();

const microserviceUrl = process.env.REACT_APP_MICROSERVICE_URL || 'http://localhost:10000'
const postgrestUrl = process.env.REACT_APP_POSTGREST_URL || 'http://localhost:5000'
const postgrestDataProvider = postgrestClient(postgrestUrl)

const hybridDataProvider = {
    ...postgrestDataProvider,
    updateMany: (resource, params) => {
        httpClient(microserviceUrl.concat('/probot/twitter/directmessage'), {
            body: JSON.stringify(params),
            headers: new Headers({
             Accept: 'application/json',
             'Content-Type': 'application/json'
            }),
            method: 'POST',
            mode: 'no-cors',
        }).then(() => {
            showNotification('Messages sent');
        }).catch((e) => {
            console.log(e); return Promise.reject(e);
        });
    }
}

export default hybridDataProvider;

