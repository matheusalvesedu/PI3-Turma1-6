
import { Request, Response } from 'express';

const express = require('express');
const app = express();

app.get('/', (req: Request, res: Response) => {
  res.send('Servidor rodando!');
});


app.listen(3000, () => console.log('Servidor iniciado na porta 3000'));