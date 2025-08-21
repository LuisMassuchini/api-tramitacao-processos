package com.camara.processos_api.service;



import com.camara.processos_api.config.FileStorageConfig;
import com.camara.processos_api.exception.FileNotFoundException;
import com.camara.processos_api.exception.FileStorageException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(FileStorageConfig fileStorageConfig) {
        // Pega o caminho do diretório de upload a partir do application.properties
        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir())
                .toAbsolutePath().normalize();

        // Cria o diretório de upload se ele não existir
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Não foi possível criar o diretório para armazenar os arquivos.", ex);
        }
    }

    /**
     * Salva o arquivo no disco.
     * @param file O arquivo enviado na requisição.
     * @param processoId O ID do processo para criar um subdiretório.
     * @return O caminho completo do arquivo salvo.
     */
    public String storeFile(MultipartFile file, Long processoId) {
        // Normaliza o nome do arquivo para remover caracteres inválidos
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        // Valida o nome do arquivo
        if (originalFileName.contains("..")) {
            throw new FileStorageException("Nome de arquivo inválido: " + originalFileName);
        }

        // Cria um diretório específico para cada processo
        Path targetLocation = this.fileStorageLocation.resolve(String.valueOf(processoId));
        try {
            Files.createDirectories(targetLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Não foi possível criar o diretório para o processo " + processoId, ex);
        }

        // Resolve o caminho final do arquivo
        Path filePath = targetLocation.resolve(originalFileName);

        try {
            // Copia o arquivo para o local de destino (sobrescreve se já existir)
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException ex) {
            throw new FileStorageException("Não foi possível salvar o arquivo " + originalFileName, ex);
        }
    }

    /**
     * Salva conteúdo vindo de um InputStream no disco.
     * @param inputStream stream com os bytes do arquivo
     * @param originalFileName nome do arquivo (usado para extensão e nome final)
     * @param processoId subdiretório do processo
     * @return caminho completo do arquivo salvo
     */
    public String storeFile(InputStream inputStream, String originalFileName, Long processoId) {
        String cleanedName = StringUtils.cleanPath(Objects.requireNonNull(originalFileName));
        if (cleanedName.contains("..")) {
            throw new FileStorageException("Nome de arquivo inválido: " + cleanedName);
        }
        Path targetLocation = this.fileStorageLocation.resolve(String.valueOf(processoId));
        try {
            Files.createDirectories(targetLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Não foi possível criar o diretório para o processo " + processoId, ex);
        }
        Path filePath = targetLocation.resolve(cleanedName);
        try {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException ex) {
            throw new FileStorageException("Não foi possível salvar o arquivo " + cleanedName, ex);
        }
    }

    /**
     * Carrega um arquivo como um Resource para download.
     * @param filePath O caminho completo do arquivo no disco.
     * @return O arquivo como um Resource.
     */
    public Resource loadFileAsResource(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("Arquivo não encontrado: " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("Arquivo não encontrado: " + filePath, ex);
        }
    }

    /**
     * Exclui um arquivo do disco.
     * @param filePath O caminho completo do arquivo.
     */
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new FileStorageException("Não foi possível excluir o arquivo: " + filePath, ex);
        }
    }
}