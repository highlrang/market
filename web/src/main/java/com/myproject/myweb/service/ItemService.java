package com.myproject.myweb.service;

import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.item.ItemRequestDto;
import com.myproject.myweb.dto.item.ItemResponseDto;
import com.myproject.myweb.dto.item.PhotoDto;
import com.myproject.myweb.repository.ItemRepository;
import com.myproject.myweb.repository.SellerNoticeRepository;
import com.myproject.myweb.repository.PhotoRepository;
import com.myproject.myweb.repository.SellerRepository;
import com.myproject.myweb.service.aws.FileUploadService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    private final SellerRepository sellerRepository;
    private final PhotoRepository photoRepository;
    private final SellerNoticeRepository sellerNoticeRepository;
    private final FileUploadService fileUploadService;

    public ItemResponseDto findById(Long id){
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));
        return new ItemResponseDto(item);
    }

    @Transactional
    public Long save(ItemRequestDto itemRequestDto){
        Seller seller = sellerRepository.findById(itemRequestDto.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));

        Item item = Item.createItem(itemRequestDto.getCategory(), seller,
                itemRequestDto.getName(), itemRequestDto.getPrice(), itemRequestDto.getStock());

        if(!itemRequestDto.getPhotos().isEmpty()) {
            List<Photo> photos = itemRequestDto.getPhotos()
                    .stream()
                    .map(PhotoDto::toEntity)
                    .collect(Collectors.toList());
            item.addPhoto(photos);
        }

        // cascade로 photo 까지 save ..
        return itemRepository.save(item).getId();
    }

    @Transactional
    public void deleteOtherPhoto(Long itemId, List<Long> photoIds) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));

        // 모든 사진 삭제 요청
        if (photoIds == null && (item.getPhotoList() != null && !item.getPhotoList().isEmpty())) {
            for (Photo photo : item.getPhotoList()) { // S3 파일 삭제
                fileUploadService.deleteS3File(photo.getName());
            }
            item.removePhoto(item.getPhotoList()); // 연관관계 매핑
            photoRepository.deleteAllInBatch();

        // 일부 사진 삭제 요청
        } else if(photoIds != null && photoIds.size() != item.getPhotoList().size()) {
            List<Photo> photos = item.getPhotoList()
                    .stream()
                    .filter(photo -> !photoIds.contains(photo.getId()))
                    .collect(Collectors.toList());

            for (Photo photo : photos) { // S3 파일 삭제
                fileUploadService.deleteS3File(photo.getName());
            }

            item.removePhoto(photos); // 연관관계 제거
            photoRepository.deleteAllInBatch(photos);
        }
    }

    @Transactional
    public void update(Long id, ItemRequestDto itemRequestDto){
        Item item = itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));

        // 사진 추가
        List<Photo> photoList = itemRequestDto.getPhotos()
                .stream()
                .map(PhotoDto::toEntity)
                .collect(Collectors.toList());

        item.update(itemRequestDto.getName(), itemRequestDto.getPrice(), itemRequestDto.getStock(), photoList);
    }

    @Transactional
    public void remove(Long id){
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("ItemNotFoundExist"));
        itemRepository.delete(item);
    }

    public ListByPaging<ItemResponseDto> findByCategory(Category category, PageRequest pageRequest){
        Page<Item> items = itemRepository.findAllByCategory(category, pageRequest);
        return new ListByPaging<>(items.getTotalPages(), items.getContent().stream()
                .map(ItemResponseDto::new)
                .collect(Collectors.toList()));
    }

    public ListByPaging<ItemResponseDto> findByCategoryAndSeller(Long sellerId, Category category, PageRequest pageRequest){
        Page<Item> items = itemRepository.findAllBySeller_IdAndCategory(sellerId, category, pageRequest);
        return new ListByPaging<>(
                items.getTotalPages(),
                items.getContent().stream()
                    .map(ItemResponseDto::new)
                    .collect(Collectors.toList())
        );
    }

    @Getter @Setter
    public static class ListByPaging<T>{
        private int totalPage;
        private List<T> list;

        public ListByPaging(int totalPage, List<T> list) {
            this.totalPage = totalPage;
            this.list = list;
        }
    }

    public void stockNotice(Long itemId){
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));
        Seller seller = item.getSeller();
        sellerNoticeRepository.save(SellerNotice.builder()
                .seller(seller)
                .title("재고 관련 알림")
                .content(item.getCategory().getValue() + "카테고리의 " + item.getName() + "상품의 재고가 소진되었습니다.")
                .build()
        );
    }
}
