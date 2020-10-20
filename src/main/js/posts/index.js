import React from 'react';
import { List, Datagrid } from 'react-admin';
import { DateField, TextField } from 'react-admin';

export const PostList = props => (
    <List {...props} perPage={100} >
        <Datagrid rowClick="show">
            <TextField source="id" label="ID"/>
            <DateField source="created_at" label="Time" />
            <TextField source="favorite_count" />
            <TextField source="retweet_count" />
            <TextField source="quoted_status_id" />
            <TextField source="retweeted_status_id" />
            <TextField source="text" label="Text"/>
        </Datagrid>
    </List>
);
