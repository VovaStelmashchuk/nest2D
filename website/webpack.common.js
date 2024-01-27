const path = require('path');

const developmentConfig = require('./config.development');
const productionConfig = require('./config.production');
const webpack = require('webpack');
const config = process.env.NODE_ENV === 'production' ? productionConfig : developmentConfig;


module.exports = {
    entry: {
        app: './js/app.js',
    },
    output: {
        path: path.resolve(__dirname, 'dist'),
        clean: true,
        filename: './js/app.js',
    },
    plugins: [
        new webpack.DefinePlugin({
            'process.env.BACKEND_HOST': JSON.stringify(config.BACKEND_HOST),
        }),
    ],
};
