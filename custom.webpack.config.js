var path = require('path');
var { merge } = require('webpack-merge');
var generated = require('./scalajs.webpack.config');
var HtmlWebpackPlugin = require('html-webpack-plugin');

var local = {
    module: {
        rules: [
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.(ttf|eot|woff|png|svg)$/,
                use: 'file-loader'
            },
            {
                test: /\.(eot)$/,
                use: 'url-loader'
            }
        ]
    },
    devServer: {
        inline: true,
        compress: true,
        proxy: {
            '/api': 'http://localhost:8080'
        }
    },
    devtool: "source-map",
    resolve: {
        alias: {
            "assets": path.resolve(__dirname, "../../../../src/main/assets")
        }
    },
    plugins: [new HtmlWebpackPlugin()]
};

module.exports = merge(generated, local);
