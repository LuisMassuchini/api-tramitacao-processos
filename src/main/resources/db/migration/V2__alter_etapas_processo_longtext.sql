-- Ajusta campos longos em etapas_processo para evitar truncamento
ALTER TABLE etapas_processo
    MODIFY COLUMN observacao LONGTEXT,
    MODIFY COLUMN assinatura LONGTEXT;

