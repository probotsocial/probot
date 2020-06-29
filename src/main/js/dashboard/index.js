import React from 'react';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import CardHeader from '@material-ui/core/CardHeader';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';

export const Dashboard = (props) => (
    <Grid container item>
        <Card>
            <CardHeader>Profile Bot (aka ProBot)</CardHeader>
            <CardContent>
                <Typography variant="h5">
                    Probot helps you manage your online accounts by:
                </Typography>
                <Typography paragraph>
                    <ul>
                    <li>Maintaining a database containing your profile, network, and activity.</li>
                    <li>Keeping track of questions and important details (like contact information) from your network.</li>
                    <li>Recognizing common types of messages, and responding accordingly.</li>
                    </ul>
                </Typography>
            </CardContent>
        </Card>
    </Grid>
);