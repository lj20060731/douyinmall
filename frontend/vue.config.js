const { defineConfig } = require('@vue/cli-service');
const webpack = require('webpack');
const path = require('path');

module.exports = defineConfig({
  transpileDependencies: true,
  configureWebpack: {
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    }
  },
  chainWebpack: config => {
    // 配置 TypeScript 支持
    config.resolve.extensions
      .add('.ts')
      .add('.tsx');

    // 配置图片加载规则，使用 asset modules
    config.module
      .rule('images')
      .test(/\.(png|jpe?g|gif|webp)(\?.*)?$/)
      .type('asset/resource')
      .set('generator', {
        filename: 'image/[name].[hash:8].[ext]'
      });

    return config;
  }
});