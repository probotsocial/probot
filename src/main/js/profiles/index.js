import React from 'react';
import { List, Datagrid } from 'react-admin';
import { ArrayField, BooleanField, DateField, NumberField, TextField, ReferenceField, SingleFieldList, ChipField, UrlField } from 'react-admin';

export const ProfileList = props => (
    <List {...props}>
        <Datagrid rowClick="show">
            <TextField source="id" label="ID"/>
            <TextField source="as_name" label="Display Name"/>
            <TextField source="apst_handle" label="Screen Name"/>
            <UrlField source="as_url" label="URL" />
            <BooleanField source="probot_following" label="Following" />
            <NumberField source="apst_followers" label="Followers" />
            <NumberField source="apst_friends" label="Friends" />
            <NumberField source="apst_lists" label="Lists" />
            <NumberField source="apst_posts" label="Posts" />
            <NumberField source="apst_likes" label="Likes" />
        </Datagrid>
    </List>
);