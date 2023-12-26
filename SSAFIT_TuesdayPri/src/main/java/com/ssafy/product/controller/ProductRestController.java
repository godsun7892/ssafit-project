package com.ssafy.product.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.product.model.dto.Product;
import com.ssafy.product.model.dto.ProductCondition;
import com.ssafy.product.model.dto.SearchCondition;
import com.ssafy.product.model.service.ProductService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api")
@Api(tags="상품 컨트롤")
//@CrossOrigin("*")
public class ProductRestController {

	@Autowired
	private ProductService productService;
	
	@Autowired
	private ResourceLoader resLoader;
	
	//1. 카테고리를 통한 데이터 가져오기
	@GetMapping("/product")
	@ApiOperation(value="상품 조회")
	public ResponseEntity<?> list(ProductCondition productCondition){
		List<Product> list = productService.getList(productCondition); //검색 조건이 있다면 그것으로 조회
		if(list == null || list.size() == 0)
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		return new ResponseEntity<List<Product>>(list, HttpStatus.OK);
	}
	
	
	
	
	//1-2. 상세보기
	@GetMapping("/product/{id}")
	public ResponseEntity<Product> detail(@PathVariable int id){
		Product product = productService.getProduct(id);
		//꼬옥 주소창을 통해 접근하려고 하는 악의무리가 있기 때문에 만약 없는 값을 보냈을때... 처리를 해주어라. (해볼것)
		return new ResponseEntity<Product>(product, HttpStatus.OK);
	}
	
	//1-3. 구매많은 것 가져오기
	@GetMapping("/product/purchase")
	public ResponseEntity<?> purchaseList(){
		List<Product> list = productService.getPurProduct(); 
		if(list == null || list.size() == 0)
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		return new ResponseEntity<List<Product>>(list, HttpStatus.OK);
	}
		
		//1-4. 판매 임박 상품 가져오기
	@GetMapping("/product/stock")
	public ResponseEntity<?> stockList(){
		List<Product> list = productService.getStockProduct();
		if(list == null || list.size() == 0)
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		return new ResponseEntity<List<Product>>(list, HttpStatus.OK);
	}
		
		//1-5. 신상품 보기
	@GetMapping("/product/new")
	public ResponseEntity<?> newList(){
		List<Product> list = productService.getNewProduct();
		if(list == null || list.size() == 0)
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		return new ResponseEntity<List<Product>>(list, HttpStatus.OK);
	}
	
	//3. 등록
	@PostMapping(value="/product",consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<?> write(@RequestPart Product product, @RequestPart(required = false) MultipartFile file) throws IllegalStateException, IOException{
		try {
			productService.registerProduct(product, file);
			return new ResponseEntity<Product>(product, HttpStatus.CREATED);
		} catch (Exception e) {
			return exceptionHandling(e);
		}
		
		//ID는 어차피 중복이 안되서 무조건 게시글이 등록이 된다.
		//문제 발생해서 게시글이 등록이 안될 경우도 있다더라.... 	
		//I / U / D 테이블의 행의 변환 개수를 반환해 주더라 이걸 이용해서 처리를 해볼 수도 있겠다....
		
	}
	
	

	//4. 삭제
	@DeleteMapping("/product/{id}")
	public ResponseEntity<Void> delete(@PathVariable int id){
		productService.removeProduct(id);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	//5. 수정
	@PutMapping("/product") //JSON 형태의 데이터로 넘어왔을 떄 처리
	public ResponseEntity<Void> update(@RequestBody Product product){
		productService.modifyProduct(product);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	//결제하기
	@PutMapping("/product/order") //JSON 형태의 데이터로 넘어왔을 떄 처리하고 싶은데?
	public ResponseEntity<Void> order(@RequestBody Product product){
		productService.orderProduct(product);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
//	@PutMapping("/board/{id}") //JSON 형태의 데이터로 넘어왔을 떄 처리하고 싶은데?
//	public ResponseEntity<Void> update(@RequestBody Board board, @PathVariable int id){
//		board.setId(id);
//		boardService.modifyBoard(board);
//		//위와같은 상황 대비
//		
//		return new ResponseEntity<Void>(HttpStatus.OK);
//	}
	
	
	
	//이미지 업로드
	private ResponseEntity<String> exceptionHandling(Exception e) {
		e.printStackTrace();
		return new ResponseEntity<String>("sorry: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@GetMapping(value="/imgfile/{productId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<?> imgFileService(@PathVariable int productId) throws IOException{
        
        System.out.println(productId);
        
        Product product = productService.getProduct(productId);
        
        Resource res = resLoader.getResource("resources/upload");
        
        File imgFile = new File(res.getFile().getCanonicalPath() + "/" + product.getImg());
        
        if(imgFile.exists()) {
            System.out.println("파일이 존재합니다.");
        } else {
            System.out.println("파일이 없습니다.");
        }
        
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(imgFile.toPath()));
        
        
                    
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentLength(resource.contentLength())
                .body(resource);

    }
	
	
	
	//1-2. 검색에 의한 데이터 가져오기
	@GetMapping("/product/search")
	@ApiOperation(value="상품 조회")
	public ResponseEntity<?> search(SearchCondition condition){
//			List<Board> list = boardService.getList(); //전체 조회
		List<Product> list = productService.search(condition); //검색 조건이 있다면 그것으로 조회
		if(list == null || list.size() == 0)
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		return new ResponseEntity<List<Product>>(list, HttpStatus.OK);
	}

}
