import React from 'react';
import {Datagrid, DateField, List, ReferenceField, TextField} from 'react-admin';

export const OptInList = props => (
    <List {...props} perPage={100} >
        <Datagrid rowClick="show">
            <TextField source="ID" />
            <ReferenceField source="user_id" label="profile" reference="followers">
                <TextField source="handle" />
            </ReferenceField>
            <DateField source="timestamp" label="Timestamp"/>
            <TextField source="emailAddress" label="Email Address"/>
            <TextField source="phoneNumber" label="Phone Number"/>
        </Datagrid>
    </List>
);