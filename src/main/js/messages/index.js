import React from 'react';
import { List, Datagrid } from 'react-admin';
import { DateField, TextField } from 'react-admin';

export const MessageList = props => (
    <List {...props} perPage={100} >
        <Datagrid rowClick="show">
            <TextField source="id" label="ID"/>
            <DateField source="timestamp" label="Timestamp" />
            <TextField source="type" label="Type"/>
            <TextField source="sender_id" />
            <TextField source="recipient_id" />
            <TextField source="text" label="Text"/>
        </Datagrid>
    </List>
);
