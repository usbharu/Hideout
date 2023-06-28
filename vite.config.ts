import {defineConfig, splitVendorChunkPlugin} from 'vite';
import solidPlugin from 'vite-plugin-solid';
import suidPlugin from "@suid/vite-plugin";
import visualizer from "rollup-plugin-visualizer";

export default defineConfig({
    plugins: [solidPlugin(),suidPlugin(),splitVendorChunkPlugin()],
    server: {
        port: 3000,
        proxy: {
            '/api': 'http://localhost:8080',
        }
    },
    root: './src/main/web',
    build: {
        target: 'esnext',
        outDir: '../resources/static',
        rollupOptions:{
            plugins: [
                visualizer()
            ]
        }
    },
});
