import React from 'react';
import { Admin, Login, ListGuesser, ShowGuesser, Resource } from 'react-admin';
import { createMuiTheme } from '@material-ui/core/styles';
import logo from './logo.svg';

import authProvider from './main/js/AuthProvider';
import dataProvider from './main/js/DataProvider';

import { MessageList } from "./main/js/messages";
import { ProfileList } from "./main/js/profiles";
import './App.css';

const LoginPage = () => <Login />;

const theme = createMuiTheme({
  palette: {
    secondary: { main: '#4E9EF6'}
  }
});

const App = () => {
  return (
      <Admin
          theme={theme}
          title="ProBot"
          // dashboard={Dashboard}
          authProvider={authProvider}
          dataProvider={dataProvider}
      >
          <Resource name="followers" list={ProfileList} show={ShowGuesser}/>
          <Resource name="messages" label="Messages" list={MessageList} show={ShowGuesser}/>
      </Admin>
  );
};

export default App;
