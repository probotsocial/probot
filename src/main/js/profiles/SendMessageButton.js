import * as React from 'react';
import { Fragment, useState } from 'react';
import {
    Button,
    Confirm,
    useUpdateMany,
    useRefresh,
    useNotify,
    useUnselectAll,
} from 'react-admin';

const SendMessageButton = ({ selectedIds }) => {
    const [open, setOpen] = useState(false);
    const refresh = useRefresh();
    const notify = useNotify();
    const unselectAll = useUnselectAll();
    const [updateMany, { loading }] = useUpdateMany(
        'profiles',
        selectedIds,
        { views: 0 },
        {
            onSuccess: () => {
                refresh();
                notify('Direct Messages queued.');
                unselectAll('profiles');
            },
            onFailure: error => notify('Error', 'warning'),
        }
    );
    const handleClick = () => setOpen(true);
    const handleDialogClose = () => setOpen(false);

    const handleConfirm = () => {
        updateMany();
        setOpen(false);
    };

    return (
        <Fragment>
            <Button label="Send Direct Message" onClick={handleClick} />
            <Confirm
                isOpen={open}
                loading={loading}
                title="Send Direct Message"
                content="Do you want to send your configured direct message to each of the selected accounts?"
                onConfirm={handleConfirm}
                onClose={handleDialogClose}
            />
        </Fragment>
    );
}

export default SendMessageButton;