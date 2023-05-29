package deleteme;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class testing {
	@GetMapping("/des")
	public ResponseEntity<String> resp(){
		return ResponseEntity.ok("Welcome sir");
	}
	
}
