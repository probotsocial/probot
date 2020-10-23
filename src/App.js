import React from 'react';
import { Admin, Login, ShowGuesser, Resource } from 'react-admin';
import { createMuiTheme } from '@material-ui/core/styles';
//import logo from './logo.svg';

import authProvider from './main/js/AuthProvider';
import dataProvider from './main/js/DataProvider';

import { Dashboard } from "./main/js/dashboard";
import { MessageList } from "./main/js/messages";
import { PostList } from "./main/js/posts";
import { ProfileList } from "./main/js/profiles";
import { OptInList } from "./main/js/optins";
import './App.css';

//console.log(require('dotenv').config())
import LoginForm from './main/js/LoginForm';

const LoginPage = (props) => <Login {...props}><LoginForm /> </Login>;

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
          dashboard={Dashboard}
          authProvider={authProvider}
          dataProvider={dataProvider}
          loginPage={LoginPage}
      >
          <Resource name="followers" label="Followers" list={ProfileList} show={ShowGuesser}/>
          <Resource name="friends" label="Following" list={ProfileList} show={ShowGuesser}/>
          <Resource name="tweets" label="Tweets" list={PostList} show={ShowGuesser}/>
          <Resource name="messages" label="Messages" list={MessageList} show={ShowGuesser}/>
          <Resource name="optins" label="Opt-Ins" list={OptInList} show={ShowGuesser}/>
      </Admin>
  );
};

export default App;
