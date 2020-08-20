import React from 'react';
import { List, Datagrid } from 'react-admin';
import { ArrayField, BooleanField, DateField, FunctionField, NumberField, TextField, ReferenceField, SingleFieldList, ChipField, UrlField } from 'react-admin';
import { Filter, TextInput, NullableBooleanInput, SearchInput } from 'react-admin';

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