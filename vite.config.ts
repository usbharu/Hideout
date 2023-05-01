import { defineConfig } from 'vite';
import solidPlugin from 'vite-plugin-solid';
import suidPlugin from "@suid/vite-plugin";

export default defineConfig({
    plugins: [solidPlugin(),suidPlugin()],
    server: {
        port: 3000,
        proxy: {
            '/api': 'http://localhost:8080',
            '/login': 'http://localhost:8080',
            '/auth-check': 'http://localhost:8080',
        }
    },
    root: './src/main/web',
    build: {
        target: 'esnext',
        outDir: '../resources/static',
    },
});
