package com.layoof.layoof.service;

import com.layoof.layoof.ai.LayoofDraft;
import com.layoof.layoof.ai.SourceDraft;
import com.layoof.layoof.dto.request.LayoofRequestDto;
import com.layoof.layoof.dto.request.LayoofResearchRequestDto;
import com.layoof.layoof.dto.request.SourceRequestDto;
import com.layoof.layoof.dto.response.LayoofDraftResponseDto;
import com.layoof.layoof.dto.response.LayoofResponseDto;
import com.layoof.layoof.dto.response.SearchLayoofResponseDto;
import com.layoof.layoof.dto.response.SourceDraftResponseDto;
import com.layoof.layoof.entity.Layoof;
import com.layoof.layoof.entity.Source;
import com.layoof.layoof.entity.User;
import com.layoof.layoof.enums.LayoofConfidence;
import com.layoof.layoof.enums.LayoofStatus;
import com.layoof.layoof.exception.*;
import com.layoof.layoof.mapper.LayoofMapper;
import com.layoof.layoof.repository.LayoofRepository;
import com.layoof.layoof.repository.SourceRepository;
import com.layoof.layoof.repository.UserRepository;
import com.layoof.layoof.uploadFile.FileUpload;
import com.layoof.layoof.uploadFile.FileUploads;
import com.layoof.layoof.uploadFile.ImageSourceRule;
import com.layoof.layoof.uploadFile.ImageUploader;
import com.layoof.layoof.util.EmailNormalizer;
import com.layoof.layoof.util.LayoofNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LayoofService {

    private static final String LAYOOF_IMAGE_FOLDER = "layoofs/";

    private final LayoofRepository layoofRepository;
    private final SourceRepository sourceRepository;
    private final LayoofMapper layoofMapper;
    private final LayoofAiService layoofAiService;
    private final ReputationService reputationService;
    private final UserRepository userRepository;
    private final ImageUploader imageUploader;

    @Transactional(readOnly = true)
    public List<LayoofResponseDto> listByAuthor(User author) {
        requireAuthenticated(author);

        return layoofMapper.toResponseList(
                layoofRepository.findByAuthorUserIdOrderByCreatedAtDesc(author.getUserId()));
    }

    @Transactional(readOnly = true)
    public List<LayoofResponseDto> listByAuthorId(UUID authorId) {
        return layoofMapper.toResponseList(
                layoofRepository.findByAuthorUserIdOrderByCreatedAtDesc(authorId));
    }

    @Transactional(readOnly = true)
    public List<SearchLayoofResponseDto> searchLayoof(String title) {

        if (title == null || title.isEmpty()) {
            return List.of();
        }

        return layoofMapper.toSearchResponseList(
                layoofRepository.searchByTitle(title.trim()));
    }

    @Transactional(readOnly = true)
    public LayoofResponseDto findById(UUID layoofId) {
        return layoofMapper.toResponse(layoofRepository.findWithSourceAndAuthorByLayoofId(layoofId)
                .orElseThrow(() -> new LayoofNotFoundException(
                        "Nenhuma demissao encontrada com o id: " + layoofId)));
    }

    public LayoofDraftResponseDto research(LayoofResearchRequestDto request, User author) {
        requireAuthenticated(author);

        LayoofDraft draft = layoofAiService.research(request.query());

        if (draft == null || draft.isEmpty()) {
            throw new LayoofNotFoundException(
                    "A IA nao encontrou nenhuma demissao para: " + request.query());
        }

        return toDraftResponse(draft);
    }

    @Transactional
    public LayoofResponseDto create(LayoofRequestDto request, MultipartFile file, User author) {
        requireAuthenticated(author);
        ImageSourceRule.requireExactlyOne(file, request.imageUrl());

        String summary = summary(request);
        String sourceUrl = requireUrl(request.sourceUrl(), "O endereco da noticia nao e uma url valida");
        requireUniqueSourceUrl(sourceUrl, null);

        if (author.getLinkedinURL() == null || author.getLinkedinURL().isBlank()) {
            throw new InvalidURLLinkedinException("A URL do LinkedIn não pode estar vazia");
        }

        userRepository.findByLinkedinURL(author.getLinkedinURL())
                .filter(owner -> !owner.getUserId().equals(author.getUserId()))
                .ifPresent(owner -> {
                    throw new InvalidURLLinkedinException("Esta conta do LinkedIn já está vinculada a outro usuário");
                });

        Layoof layoof = new Layoof();
        apply(layoof, request, sourceUrl, summary);
        layoof.setAuthor(author);
        layoof.setStatus(LayoofStatus.PUBLISHED);

        Layoof created = layoofRepository.saveAndFlush(layoof);

        if (ImageSourceRule.hasFile(file)) {
            created.setImageUrl(imageUploader.upload(
                    FileUploads.from(file), LAYOOF_IMAGE_FOLDER + created.getLayoofId()));
        }

        return layoofMapper.toResponse(created);
    }

    @Transactional
    public LayoofResponseDto update(UUID layoofId, LayoofRequestDto request, User author) {
        requireAuthenticated(author);

        String summary = summary(request);
        Layoof layoof = requireAuthor(findEntityById(layoofId), author,
                "Voce so pode editar as demissoes que voce mesmo cadastrou");

        String previousImage = layoof.getImageUrl();

        String sourceUrl = requireUrl(request.sourceUrl(), "O endereco da noticia nao e uma url valida");
        requireUniqueSourceUrl(sourceUrl, layoofId);

        apply(layoof, request, sourceUrl, summary);

        if (!Objects.equals(previousImage, layoof.getImageUrl())) {
            imageUploader.deleteByUrl(previousImage);
        }

        return layoofMapper.toResponse(layoof);
    }

    @Transactional
    public LayoofResponseDto updateImage(UUID layoofId, User author, FileUpload file) {
        requireAuthenticated(author);

        Layoof layoof = requireAuthor(findEntityById(layoofId), author,
                "Voce so pode trocar a imagem das demissoes que voce mesmo cadastrou");

        String imageUrl = imageUploader.upload(file, LAYOOF_IMAGE_FOLDER + layoof.getLayoofId());

        imageUploader.deleteByUrl(layoof.getImageUrl());
        layoof.setImageUrl(imageUrl);

        return layoofMapper.toResponse(layoof);
    }

    @Transactional
    public void delete(UUID layoofId, User author) {
        requireAuthenticated(author);

        Layoof layoof = requireAuthor(findEntityById(layoofId), author,
                "Voce so pode apagar as demissoes que voce mesmo cadastrou");

        imageUploader.deleteByUrl(layoof.getImageUrl());
        layoofRepository.delete(layoof);
        layoofRepository.flush();

        reputationService.refresh(author);
    }

    private void apply(Layoof layoof, LayoofRequestDto request, String sourceUrl, String summary) {
        String company = LayoofNormalizer.company(request.company());
        String title = LayoofNormalizer.text(request.title());

        layoof.setCompany(company);
        layoof.setTitle(title);
        layoof.setNumbersOfCuts(LayoofNormalizer.cuts(request.numbersOfCuts()));
        layoof.setCity(LayoofNormalizer.text(request.city()));
        layoof.setCountry(LayoofNormalizer.country(request.country()));
        layoof.setSummary(summary);
        layoof.setContent(content(request.content()));
        layoof.setImageUrl(LayoofNormalizer.canonicalUrl(request.imageUrl()));
        layoof.setSourceUrl(sourceUrl);
        layoof.setPublishedAt(request.publishedAt() == null ? LocalDateTime.now() : request.publishedAt());
        layoof.setTitleFingerprint(LayoofNormalizer.fingerprint(company, title));
        layoof.setSource(resolveSource(request.source()));
    }

    private String summary(LayoofRequestDto request) {
        String summary = LayoofNormalizer.text(request.summary());

        if (summary != null) {
            return summary;
        }

        return LayoofNormalizer.text(layoofAiService.summarize(
                request.company(), request.title(), request.content()));
    }

    private Source resolveSource(SourceRequestDto request) {
        String feedUrl = requireUrl(request.feedUrl(), "O endereco do veiculo nao e uma url valida");

        Optional<Source> catalogued = sourceRepository.findByFeedUrl(feedUrl);
        if (catalogued.isPresent()) {
            Source source = catalogued.get();
            return source.isActive() ? source : sourceRepository.save(apply(source, request, feedUrl));
        }

        Source source = new Source();
        source.setActive(false);

        return sourceRepository.save(apply(source, request, feedUrl));
    }

    private Source apply(Source source, SourceRequestDto request, String feedUrl) {
        source.setName(LayoofNormalizer.text(request.name()));
        source.setFeedUrl(feedUrl);
        source.setType(request.type());
        source.setLanguage(LayoofNormalizer.text(request.language()));
        source.setRegion(LayoofNormalizer.text(request.region()));
        source.setDescription(LayoofNormalizer.text(request.description()));
        return source;
    }

    private LayoofDraftResponseDto toDraftResponse(LayoofDraft draft) {
        return new LayoofDraftResponseDto(
                LayoofNormalizer.company(draft.company()),
                LayoofNormalizer.text(draft.title()),
                LayoofNormalizer.cuts(draft.numbersOfCuts()),
                LayoofNormalizer.text(draft.city()),
                LayoofNormalizer.country(draft.country()),
                LayoofNormalizer.text(draft.summary()),
                content(draft.content()),
                LayoofNormalizer.canonicalUrl(draft.imageUrl()),
                LayoofNormalizer.canonicalUrl(draft.sourceUrl()),
                LayoofNormalizer.publishedAt(draft.publishedAt()),
                draft.confidence() == null ? LayoofConfidence.LOW : draft.confidence(),
                toDraftResponse(draft.source()));
    }

    private SourceDraftResponseDto toDraftResponse(SourceDraft source) {
        if (source == null) {
            return null;
        }

        return new SourceDraftResponseDto(
                LayoofNormalizer.text(source.name()),
                LayoofNormalizer.canonicalUrl(source.feedUrl()),
                source.type(),
                LayoofNormalizer.text(source.language()),
                LayoofNormalizer.text(source.region()),
                LayoofNormalizer.text(source.description()));
    }

    private void requireUniqueSourceUrl(String sourceUrl, UUID layoofId) {
        boolean duplicated = layoofId == null
                ? layoofRepository.existsBySourceUrl(sourceUrl)
                : layoofRepository.existsBySourceUrlAndLayoofIdNot(sourceUrl, layoofId);

        if (duplicated) {
            throw new LayoofAlreadyExistsException(
                    "Ja existe uma demissao cadastrada com o endereco: " + sourceUrl);
        }
    }

    private String requireUrl(String value, String message) {
        String canonical = LayoofNormalizer.canonicalUrl(value);

        if (canonical == null) {
            throw new InvalidLayoofDataException(message);
        }
        return canonical;
    }

    private String content(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private Layoof findEntityById(UUID layoofId) {
        return layoofRepository.findById(layoofId)
                .orElseThrow(() -> new LayoofNotFoundException(
                        "Nenhuma demissao encontrada com o id: " + layoofId));
    }

    private Layoof requireAuthor(Layoof layoof, User author, String message) {
        if (!layoof.isAuthoredBy(author)) {
            throw new LayoofNotOwnedException(message);
        }
        return layoof;
    }

    private void requireAuthenticated(User author) {
        if (author == null) {
            throw new UnauthenticatedException("E preciso estar autenticado para gerenciar demissoes");
        }
    }

    @Transactional(readOnly = true)
    public List<LayoofResponseDto> list(LayoofStatus status) {
        return layoofMapper.toResponseList(status == null
                ? layoofRepository.findAllByOrderByPublishedAtDesc()
                : layoofRepository.findByStatusOrderByPublishedAtDesc(status));
    }

    public long count() {
        return layoofRepository.count();
    }

}
