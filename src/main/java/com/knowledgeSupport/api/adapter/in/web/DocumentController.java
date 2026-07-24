package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.application.port.in.DeleteDocumentUseCase;
import com.knowledgeSupport.api.application.port.in.GetDocumentChunksUseCase;
import com.knowledgeSupport.api.application.port.in.ListDocumentsUseCase;
import com.knowledgeSupport.api.application.port.in.RelatedCalledsUseCase;
import com.knowledgeSupport.api.application.port.in.SearchChunksUseCase;
import com.knowledgeSupport.api.application.port.in.UploadDocumentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;

/**
 * Documentation vertical: upload/list/delete manuals (PDF/DOCX), inspect what got extracted,
 * and search across them (TF-IDF keyword search — see docs/ for the "why not embeddings yet").
 */
@RestController
@RequestMapping("/api/documentacao")
@Tag(name = "Documentação", description = "Indexed manuals (PDF/DOCX), shared across the whole support team — keyword search over their extracted text.")
public class DocumentController {

    private final UploadDocumentUseCase uploadDocumentUseCase;
    private final ListDocumentsUseCase listDocumentsUseCase;
    private final DeleteDocumentUseCase deleteDocumentUseCase;
    private final GetDocumentChunksUseCase getDocumentChunksUseCase;
    private final SearchChunksUseCase searchChunksUseCase;
    private final RelatedCalledsUseCase relatedCalledsUseCase;

    public DocumentController(UploadDocumentUseCase uploadDocumentUseCase,
                               ListDocumentsUseCase listDocumentsUseCase,
                               DeleteDocumentUseCase deleteDocumentUseCase,
                               GetDocumentChunksUseCase getDocumentChunksUseCase,
                               SearchChunksUseCase searchChunksUseCase,
                               RelatedCalledsUseCase relatedCalledsUseCase) {
        this.uploadDocumentUseCase = uploadDocumentUseCase;
        this.listDocumentsUseCase = listDocumentsUseCase;
        this.deleteDocumentUseCase = deleteDocumentUseCase;
        this.getDocumentChunksUseCase = getDocumentChunksUseCase;
        this.searchChunksUseCase = searchChunksUseCase;
        this.relatedCalledsUseCase = relatedCalledsUseCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Uploads and indexes a document",
            description = "Synchronous: extracts the text, chunks it (~700 chars, word-boundary safe) and indexes "
                    + "it before responding. A parse failure doesn't fail the request — it comes back with "
                    + "status 'falhou' and the error message, so the caller can still list/retry.")
    @ApiResponse(responseCode = "200", description = "Indexing finished (check 'status' for success/failure)")
    public DocumentMetaResponse upload(@RequestParam("arquivo") MultipartFile arquivo) {
        return DocumentMetaResponse.from(uploadDocumentUseCase.upload(arquivo.getOriginalFilename(), readBytes(arquivo)));
    }

    @GetMapping
    @Operation(summary = "Lists every indexed document")
    @ApiResponse(responseCode = "200", description = "List of documents (may be empty)")
    public List<DocumentMetaResponse> list() {
        return listDocumentsUseCase.listAll().stream().map(DocumentMetaResponse::from).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Removes a document and its indexed chunks")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Document removed"),
            @ApiResponse(responseCode = "404", description = "No document with that id")
    })
    public void delete(@PathVariable UUID id) {
        deleteDocumentUseCase.deleteById(id);
    }

    @GetMapping("/{id}/trechos")
    @Operation(summary = "Chunks extracted from a document, in extraction order",
            description = "For manually checking what the indexing captured — e.g. spotting a scanned PDF with no real text layer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of chunks (may be empty)"),
            @ApiResponse(responseCode = "404", description = "No document with that id")
    })
    public List<TrechoDocumentoResponse> trechos(@PathVariable UUID id) {
        return getDocumentChunksUseCase.chunksOf(id).stream().map(TrechoDocumentoResponse::from).toList();
    }

    @PostMapping("/buscar")
    @Operation(summary = "Searches relevant chunks for a free-text query",
            description = "TF-IDF keyword search across every indexed document. Top 3, ranked by relevance desc.")
    @ApiResponse(responseCode = "200", description = "Top matches (may be empty if nothing scored)")
    public List<TrechoEncontradoResponse> buscar(@RequestBody BuscarRequest request) {
        return searchChunksUseCase.search(request.consulta()).stream().map(TrechoEncontradoResponse::from).toList();
    }

    @GetMapping("/{id}/chamados-relacionados")
    @Operation(summary = "Open Jira tickets this document likely resolves",
            description = "Scores every open ticket's title+errorName against this document's own text — the "
                    + "caller doesn't send a ticket list, the API already has Jira access via /api/calleds.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Related tickets, ranked by relevance desc (may be empty)"),
            @ApiResponse(responseCode = "404", description = "No document with that id")
    })
    public List<ChamadoRelacionadoResponse> chamadosRelacionados(@PathVariable UUID id) {
        return relatedCalledsUseCase.relatedCalleds(id).stream().map(ChamadoRelacionadoResponse::from).toList();
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo enviado: " + file.getOriginalFilename(), e);
        }
    }
}
