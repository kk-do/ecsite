package jp.co.internous.ecsite.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import jp.co.internous.ecsite.model.dao.GoodsRepository;
import jp.co.internous.ecsite.model.dao.PurchaseRepository;
import jp.co.internous.ecsite.model.dao.UserRepository;
import jp.co.internous.ecsite.model.dto.HistoryDto;
import jp.co.internous.ecsite.model.dto.LoginDto;
import jp.co.internous.ecsite.model.entity.Goods;
import jp.co.internous.ecsite.model.entity.Purchase;
import jp.co.internous.ecsite.model.entity.User;
import jp.co.internous.ecsite.model.form.Cartform;
import jp.co.internous.ecsite.model.form.HistoryForm;
import jp.co.internous.ecsite.model.form.LoginForm;

@Controller
@RequestMapping("/ecsite")
public class IndexController {
	
	//UserエンティティからUserテーブルにアクセスするDAO
	@Autowired
	private UserRepository userRepos;

	//GoodsエンティティからgoodsテーブルにアクセスするDAO
	@Autowired
	private GoodsRepository goodsRepos;
	
	//購入処理のメソッド
	@Autowired
	private PurchaseRepository purchaseRepos;
	
	//Gsonをインスタンス化（JSON形式を使えるようにするため）
	private Gson gson = new Gson();
	
	//トップページindex.htmlに移るメソッド。
	//goodsテーブルから取得した商品エンティティの一覧をフロントに渡すModelに追加していく。
	@RequestMapping("/")
	public String index(Model m) {
		List<Goods> goods = goodsRepos.findAll();
		m.addAttribute("goods",goods);
		
		return "index";
	}
	@ResponseBody
	@PostMapping("/api/login")
	public String loginApi(@RequestBody LoginForm form) {
		List<User> users = userRepos.findByUserNameAndPassword(form.getUserName(),form.getPassword());
	
	     LoginDto dto = new LoginDto(0, null, null, "ゲスト");
	     if(users.size() > 0) {
		      dto = new LoginDto(users.get(0));
	     }
	     return gson.toJson(dto);
	}
	
	@ResponseBody
	@PostMapping("/api/purchase")
	public String purchaseApi(@RequestBody Cartform f) {
		
		f.getCartList().forEach((c) -> {
			long total = c.getPrice() * c.getCount();
			purchaseRepos.persist(f.getUserId(), c.getId(), c.getGoodsName(), c.getCount(), total);
		});
		return String.valueOf(f.getCartList().size());
	}

	@ResponseBody
	@PostMapping("/api/history")
	public String historyApi(@RequestBody HistoryForm form) {
		String userId = form.getUserId();
		List<Purchase> history = purchaseRepos.findHistory(Long.parseLong(userId));
		List<HistoryDto> historyDtoList = new ArrayList<>();
		history.forEach((v) -> {
			HistoryDto dto = new HistoryDto(v);
			historyDtoList.add(dto);
		});
		System.out.println(historyDtoList.get(0).getGoodsName());
		return gson.toJson(historyDtoList);
	}
}