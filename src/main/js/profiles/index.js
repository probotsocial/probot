import React from 'react';
import { List, Datagrid } from 'react-admin';
import { ArrayField, BooleanField, DateField, NumberField, TextField, ReferenceField, SingleFieldList, ChipField, UrlField } from 'react-admin';
import { Filter, TextInput, NullableBooleanInput, SearchInput } from 'react-admin';
import SendMessageButton from './SendMessageButton'

export const ProfileFilter = (props) => (
    <Filter {...props}>
        <TextInput label="Screen Name" source="apst_handle" allowEmpty />
        <SearchInput label="Display Name" source="as_name@ilike" allowEmpty />
        <SearchInput label="Bio" source="as_summary@ilike" allowEmpty />
        <SearchInput label="Location" source="as_location_name@ilike" allowEmpty />
        <NullableBooleanInput label="Following" source="twitter_following" displayNull />
    </Filter>
);

export const ProfileList = props => (
    <List {...props} perPage={100} filters={<ProfileFilter/>} bulkActionButtons={<SendMessageButton />} >
        <Datagrid rowClick="show">
            <TextField source="id" label="ID"/>
            <TextField source="apst_handle" label="Screen Name"/>
            <TextField source="as_name" label="Display Name"/>
            <TextField source="as_location_name" label="Location"/>
            <UrlField source="as_url" label="URL" />
            <BooleanField source="twitter_following" label="Following" />
            <NumberField source="apst_followers" label="Followers" />
            <NumberField source="apst_friends" label="Friends" />
            <NumberField source="apst_lists" label="Lists" />
            <NumberField source="apst_posts" label="Posts" />
            <NumberField source="apst_likes" label="Likes" />
        </Datagrid>
    </List>
);