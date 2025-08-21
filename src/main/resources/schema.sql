-- Ajustes de colunas para suportar textos longos
-- Processos
ALTER TABLE processos_tramitacao
    MODIFY COLUMN descricao LONGTEXT;

-- Etapas
ALTER TABLE etapas_processo
    MODIFY COLUMN observacao LONGTEXT,
    MODIFY COLUMN assinatura LONGTEXT;

