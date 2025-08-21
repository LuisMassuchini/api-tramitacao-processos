package com.camara.processos_api.service;

import com.camara.processos_api.model.Etapa;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.kernel.colors.DeviceGray;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class PdfGenerationService {

    /**
     * Gera um PDF de uma única página com o conteúdo da etapa (para histórico individual).
     */
    public File gerarPdfDeEtapa(Etapa etapa) throws IOException {
        String nomeArquivo = "despacho_etapa_" + etapa.getId() + ".pdf";
        File pdfFile = new File(System.getProperty("java.io.tmpdir"), nomeArquivo);

        try (PdfDocument pdfDoc = new PdfDocument(new PdfWriter(pdfFile));
             Document document = new Document(pdfDoc)) {
            preencherPaginaDespacho(document, etapa, etapa.getAssinatura(), false);
        }
        return pdfFile;
    }

    /**
     * Gera PDF da etapa usando assinatura fornecida (sem precisar persistir Base64)
     */
    public File gerarPdfDeEtapa(Etapa etapa, String assinaturaBase64) throws IOException {
        // preserva compatibilidade chamando com usarAssinaturaTexto=false
        return gerarPdfDeEtapa(etapa, assinaturaBase64, false);
    }

    // NOVO: versão com flag para assinatura padronizada em texto
    public File gerarPdfDeEtapa(Etapa etapa, String assinaturaBase64, boolean usarAssinaturaTexto) throws IOException {
        String nomeArquivo = "despacho_etapa_" + etapa.getId() + ".pdf";
        File pdfFile = new File(System.getProperty("java.io.tmpdir"), nomeArquivo);
        try (PdfDocument pdfDoc = new PdfDocument(new PdfWriter(pdfFile));
             Document document = new Document(pdfDoc)) {
            preencherPaginaDespacho(document, etapa, assinaturaBase64, usarAssinaturaTexto);
        }
        return pdfFile;
    }

    /**
     * Adiciona uma nova página de despacho a um PDF existente, ou cria um novo se não houver.
     * Assinatura (imagem base64) é obtida da própria etapa.
     */
    public File adicionarPaginaDeDespacho(File pdfExistente, Etapa etapa) throws IOException {
        return adicionarPaginaDeDespacho(pdfExistente, etapa, etapa.getAssinatura(), false);
    }

    /**
     * Adiciona uma nova página de despacho a um PDF existente, ou cria um novo se não houver.
     * Compatibilidade com chamadas antigas: usarAssinaturaTexto=false por padrão.
     */
    public File adicionarPaginaDeDespacho(File pdfExistente, Etapa etapa, String assinaturaBase64) throws IOException {
        return adicionarPaginaDeDespacho(pdfExistente, etapa, assinaturaBase64, false);
    }

    /**
     * Adiciona uma nova página de despacho a um PDF existente, ou cria um novo se não houver.
     * @param pdfExistente O arquivo PDF anterior (pode ser nulo se for a primeira etapa).
     * @param etapa A nova etapa com os dados a serem adicionados.
     * @param assinaturaBase64 A imagem da assinatura desenhada, codificada em Base64 (pode ser null).
     * @param usarAssinaturaTexto Se true, imprime bloco de assinatura padronizado em texto.
     * @return Um novo arquivo File contendo o PDF atualizado.
     */
    public File adicionarPaginaDeDespacho(File pdfExistente, Etapa etapa, String assinaturaBase64, boolean usarAssinaturaTexto) throws IOException {
        // Alinha com padrão utilizado no projeto/BD: despacho_processo_{id}.pdf
        String nomeArquivo = "despacho_processo_" + etapa.getProcesso().getId() + ".pdf";
        File novoPdfFile = new File(System.getProperty("java.io.tmpdir"), nomeArquivo);

        // Cria/abre o documento de destino
        try (PdfDocument destino = new PdfDocument(new PdfWriter(novoPdfFile))) {
            // 1) Copia todas as páginas do PDF existente (se existir)
            if (pdfExistente != null && pdfExistente.exists()) {
                try (PdfDocument origemAntiga = new PdfDocument(new PdfReader(pdfExistente))) {
                    if (origemAntiga.getNumberOfPages() > 0) {
                        origemAntiga.copyPagesTo(1, origemAntiga.getNumberOfPages(), destino);
                    }
                }
            }
            int oldPageCount = destino.getNumberOfPages();

            // 2) Gera um PDF de 1 página para a nova etapa e copia essa(s) página(s)
            File paginaNova = gerarPdfDeEtapa(etapa, assinaturaBase64, usarAssinaturaTexto);
            try (PdfDocument origemNova = new PdfDocument(new PdfReader(paginaNova))) {
                if (origemNova.getNumberOfPages() > 0) {
                    origemNova.copyPagesTo(1, origemNova.getNumberOfPages(), destino);
                }
            } finally {
                // Limpa o PDF temporário da página nova
                //noinspection ResultOfMethodCallIgnored
                paginaNova.delete();
            }

            // 3) Se houver protocolo, carimba nas páginas antigas (1..oldPageCount)
            String protocolo = etapa.getProcesso().getProtocolo();
            if (protocolo != null && !protocolo.isBlank() && oldPageCount > 0) {
                String textoCarimbo = "Processo nº: " + protocolo;
                for (int i = 1; i <= oldPageCount; i++) {
                    var page = destino.getPage(i);
                    Rectangle pageSize = page.getPageSize();
                    float x = pageSize.getWidth() / 2f;
                    float y = pageSize.getTop() - 24f; // 24pt da borda superior
                    try (Canvas canvas = new Canvas(new PdfCanvas(page), pageSize)) {
                        canvas.setFontSize(9f)
                              .setFontColor(DeviceGray.BLACK)
                              .showTextAligned(textoCarimbo, x, y, TextAlignment.CENTER);
                    }
                }
            }
        }

        return novoPdfFile;
    }

    // Tenta carregar o logo do classpath em /static/logo_prefeitura.png (fallback: /logo_prefeitura.png)
    private Image carregarLogoCabecalho() {
        String[] caminhos = new String[]{"/static/logo_prefeitura.png", "/logo_prefeitura.png"};
        for (String path : caminhos) {
            try (InputStream is = PdfGenerationService.class.getResourceAsStream(path)) {
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    Image img = new Image(ImageDataFactory.create(bytes));
                    img.scaleToFit(48, 48); // logo menor
                    img.setHorizontalAlignment(HorizontalAlignment.LEFT);
                    return img;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void preencherPaginaDespacho(Document document, Etapa etapa, String assinaturaBase64, boolean usarAssinaturaTexto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        Image logo = carregarLogoCabecalho();
        if (logo != null) {
            Table header = new Table(new float[]{1, 4});
            header.setWidth(UnitValue.createPercentValue(100));
            Cell cLogo = new Cell().setBorder(Border.NO_BORDER);
            logo.setHorizontalAlignment(HorizontalAlignment.LEFT);
            cLogo.add(logo);
            Cell cTitulos = new Cell().setBorder(Border.NO_BORDER);
            cTitulos.add(new Paragraph("CÂMARA MUNICIPAL DE JACAREÍ").setBold().setTextAlignment(TextAlignment.LEFT));
            cTitulos.add(new Paragraph("PALÁCIO DA LIBERDADE").setBold().setTextAlignment(TextAlignment.LEFT));
            header.addCell(cLogo);
            header.addCell(cTitulos);
            document.add(header);
        } else {
            document.add(new Paragraph("CÂMARA MUNICIPAL DE JACAREÍ").setTextAlignment(TextAlignment.CENTER).setBold());
            document.add(new Paragraph("PALÁCIO DA LIBERDADE").setTextAlignment(TextAlignment.CENTER).setBold());
        }
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("DESPACHO DE TRAMITAÇÃO")
                .setTextAlignment(TextAlignment.CENTER).setBold().setUnderline().setFontSize(14));

        // Mostrar o número do processo apenas se existir protocolo (sem fallback de ID)
        String protocolo = etapa.getProcesso().getProtocolo();
        if (protocolo != null && !protocolo.isBlank()) {
            document.add(new Paragraph("Processo nº: " + protocolo)
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(10));
        }
        document.add(new Paragraph("\n\n"));

        document.add(new Paragraph().add(new Text("Ação: ").setBold()).add(etapa.getStatus()));
        document.add(new Paragraph().add(new Text("Data/Hora: ").setBold()).add(etapa.getDataEnvio().format(formatter)));
        document.add(new Paragraph().add(new Text("De: ").setBold()).add(etapa.getDeUsuario().getNome() + " (" + etapa.getDeDepartamento() + ")"));
        document.add(new Paragraph().add(new Text("Para: ").setBold()).add(etapa.getParaUsuario().getNome() + " (" + etapa.getParaDepartamento() + ")"));
        document.add(new Paragraph().add(new Text("Observação: ").setBold()).add(etapa.getObservacao() != null && !etapa.getObservacao().isEmpty() ? etapa.getObservacao() : "Nenhuma."));

        document.add(new Paragraph("\n\n\n"));

        boolean assinaturaDesenhada = false;
        if (assinaturaBase64 != null && !assinaturaBase64.isEmpty()) {
            try {
                String base64Image = assinaturaBase64.contains(",") ? assinaturaBase64.split(",")[1] : assinaturaBase64;
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                Image img = new Image(ImageDataFactory.create(imageBytes));
                img.scaleToFit(220, 110);
                img.setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(img);
                assinaturaDesenhada = true;
            } catch (Exception e) {
                System.err.println("Erro ao decodificar/adicionar imagem da assinatura: " + e.getMessage());
            }
        }

        // Se não houver imagem e a opção de texto estiver marcada, imprime bloco padronizado
        if (!assinaturaDesenhada && usarAssinaturaTexto) {
            document.add(new Paragraph("Documento assinado eletronicamente por meio do Sistema Interno de Comunicação (SICOM) por:")
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(10).setFontColor(DeviceGray.GRAY));
            document.add(new Paragraph(etapa.getDeUsuario().getNome().toUpperCase())
                    .setTextAlignment(TextAlignment.CENTER).setBold());
            document.add(new Paragraph("Matrícula: " + (etapa.getDeUsuario().getMatricula() != null ? etapa.getDeUsuario().getMatricula() : ""))
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(11));
            document.add(new Paragraph("Data e hora da assinatura: " + etapa.getDataEnvio().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy 'às' HH:mm:ss")))
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(11));
        }

        // Linha de assinatura sempre presente (abaixo da imagem ou do bloco de texto)
        SolidLine line = new SolidLine(1f);
        LineSeparator ls = new LineSeparator(line);
        ls.setWidth(UnitValue.createPointValue(240));
        ls.setHorizontalAlignment(HorizontalAlignment.CENTER);
        if (!assinaturaDesenhada && !usarAssinaturaTexto) {
            document.add(new Paragraph("\n"));
        }
        document.add(ls);

        document.add(new Paragraph(etapa.getDeUsuario().getNome().toUpperCase())
                .setTextAlignment(TextAlignment.CENTER).setBold().setMarginTop(2));
        document.add(new Paragraph("Departamento: " + (etapa.getDeUsuario().getDepartamento() != null ? etapa.getDeUsuario().getDepartamento() : ""))
                .setTextAlignment(TextAlignment.CENTER).setFontSize(11));
    }
}
