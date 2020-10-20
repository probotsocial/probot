import {Auth0Client} from '@auth0/auth0-spa-js';

const auth0 = new Auth0Client({
    domain: process.env.REACT_APP_AUTH0_DOMAIN || "dev-probot.us.auth0.com" ,
    client_id: process.env.REACT_APP_AUTH0_CLIENTID || "FO0NwBs3xKa4o8a2E8mQ2PF80d4bZRn0",
    redirect_uri: window.location.origin,
    cacheLocation: 'localstorage',
    useRefreshTokens: true,
    sameSite: 'none',
});

export default {
    // called when the user attempts to log in
    login: (url) => {
        if (typeof url === 'undefined') {
          return auth0.loginWithRedirect({
            redirect_uri: window.location.origin
          }).then(token => {
            //logged in. you can get the user profile like this:
            auth0.getUser().then(user => {
              console.log(user);
            });
          });
        }
        return auth0.handleRedirectCallback(url.location);
    },
    // called when the user clicks on the logout button
    logout: () => {
        return auth0.isAuthenticated().then(function (isAuthenticated) {
            if (isAuthenticated) { // need to check for this as react-admin calls logout in case checkAuth failed
                return auth0.logout({
                    redirect_uri: window.location.origin,
                    federated: true // have to be enabled to invalidate refresh token
                });
            }
            return Promise.resolve()
        })
    },
    // called when the API returns an error
    checkError: ({status}) => {
        if (status === 401 || status === 403) {
            return Promise.reject();
        }
        return Promise.resolve();
    },
    // called when the user navigates to a new location, to check for authentication
    checkAuth: () => {
        auth0.getUser().then(function(){console.log(arguments)});
        return auth0.isAuthenticated().then(function (isAuthenticated) {
            console.log(isAuthenticated);
            if (isAuthenticated) {
                return Promise.resolve();
            }
            return auth0.getTokenSilently()
        })
    },
    // called when the user navigates to a new location, to check for permissions / roles
    getPermissions: () => {
        return Promise.resolve()
    },
};
