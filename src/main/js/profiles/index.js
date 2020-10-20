import React from 'react';
import { List, Datagrid } from 'react-admin';
import { BooleanField, NumberField, TextField, UrlField } from 'react-admin';
import { Filter, TextInput, NullableBooleanInput, SearchInput } from 'react-admin';
import SendMessageButton from './SendMessageButton'

export const ProfileFilter = (props) => (
    <Filter {...props}>
        <TextInput label="Screen Name" source="screen_name" allowEmpty />
        <SearchInput label="Display Name" source="name@ilike" allowEmpty />
        <SearchInput label="Bio" source="description@ilike" allowEmpty />
        <SearchInput label="Location" source="location@ilike" allowEmpty />
        <NullableBooleanInput label="Following" source="following" displayNull />
    </Filter>
);

export const ProfileList = props => (
    <List {...props} perPage={100} filters={<ProfileFilter/>} bulkActionButtons={<SendMessageButton />} >
        <Datagrid rowClick="show">
            <TextField source="id" label="ID"/>
            <TextField source="screen_name" label="Screen Name"/>
            <TextField source="name" label="Display Name"/>
            <TextField source="location" label="Location"/>
            <UrlField source="url" label="URL" />
            <BooleanField source="following" label="Following" />
            <NumberField source="followers_count" label="Followers" />
            <NumberField source="friends_count" label="Friends" />
            <NumberField source="lists_count" label="Lists" />
            <NumberField source="statuses_count" label="Posts" />
            <NumberField source="favourites_count" label="Likes" />
        </Datagrid>
    </List>
);
