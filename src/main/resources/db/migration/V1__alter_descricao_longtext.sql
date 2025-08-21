-- Altera a coluna descricao para LONGTEXT, evitando truncamento em textos grandes
ALTER TABLE processos_tramitacao
    MODIFY COLUMN descricao LONGTEXT;

