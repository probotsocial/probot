import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import { Auth0Provider } from "@auth0/auth0-react";

const auth0Domain = process.env.REACT_APP_AUTH0_DOMAIN || 'dev-probot.us.auth0.com';
const auth0ClientID = process.env.REACT_APP_AUTH0_CLIENTID || 'K6GoUSOrXgsWQguk7j6G6HXO9SzRaVXp';

ReactDOM.render(
  <Auth0Provider
    domain={auth0Domain}
    clientId={auth0ClientID}
    redirectUri={window.location.origin}
    audience="https://dev-probot.us.auth0.com/api/v2/"
  >
    <React.StrictMode>
      <App />
    </React.StrictMode>
  </Auth0Provider>,
  document.getElementById("root")
);
