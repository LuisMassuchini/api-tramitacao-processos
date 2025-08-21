package com.camara.processos_api.service;

import com.camara.processos_api.model.Arquivo;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipService {

    public byte[] criarZipDeArquivos(Collection<Arquivo> arquivos) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // NOVO: Usamos um Set para controlar os nomes de arquivos já adicionados ao zip.
        Set<String> nomesAdicionados = new HashSet<>();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Arquivo arquivo : arquivos) {
                String nomeOriginal = arquivo.getNomeArquivo();
                String nomeFinal = nomeOriginal;
                int contador = 1;

                // LÓGICA DE VERIFICAÇÃO DE DUPLICIDADE:
                // Enquanto o nome final já existir no zip, tenta um novo nome.
                while (nomesAdicionados.contains(nomeFinal)) {
                    // Pega o nome sem a extensão
                    String nomeBase = nomeOriginal.substring(0, nomeOriginal.lastIndexOf('.'));
                    // Pega a extensão
                    String extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf('.'));
                    // Cria um novo nome: "nome (1).txt"
                    nomeFinal = String.format("%s (%d)%s", nomeBase, contador++, extensao);
                }

                // Adiciona o nome final (e único) ao controle e ao zip.
                nomesAdicionados.add(nomeFinal);
                ZipEntry entry = new ZipEntry(nomeFinal);
                zos.putNextEntry(entry);

                File file = new File(arquivo.getCaminhoArquivo());
                if (!file.exists()) {
                    System.err.println("[WARN] Arquivo ausente no disco, ignorando no ZIP: " + arquivo.getCaminhoArquivo());
                    continue;
                }

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }
}