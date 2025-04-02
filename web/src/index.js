"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const express = require('express');
const app = express();
app.get('/', (req, res) => {
    res.send('Servidor rodando!');
});
app.listen(3000, () => console.log('Servidor iniciado na porta 3000'));
