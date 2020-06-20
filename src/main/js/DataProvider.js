import postgrestClient from 'ra-data-postgrest';

const dataProvider = [
    { dataProvider: postgrestClient('http://localhost:5000'),
        resources: [
            'followers'
        ]
    }
];

