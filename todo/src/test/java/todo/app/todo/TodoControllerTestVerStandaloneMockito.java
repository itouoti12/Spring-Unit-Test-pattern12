package todo.app.todo;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.terasoluna.gfw.common.exception.BusinessException;
import org.terasoluna.gfw.common.message.ResultMessage;
import org.terasoluna.gfw.common.message.ResultMessages;

import todo.domain.model.Todo;
import todo.domain.service.todo.TodoService;

/**
 * Controller Test
 * mockitoによるServiceメソッドのモック化
 * standaloneSetup
 */
public class TodoControllerTestVerStandaloneMockito {

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();
	
	@InjectMocks
	TodoController target;
	
	@Mock
	TodoService todoService;
	
	MockMvc mockMvc;
	
	@Before
	public void setUp() {
		//standaloneモードでmockMvcを起動
		mockMvc = MockMvcBuilders.standaloneSetup(target).build();
	}
	
	//正常動作のパターン
	@Test
	public void testFinish() throws Exception {
		
		//mockを作成（getTodoTrueDataメソッドでモック用のデータを返している）
		when(todoService.finish(anyString())).thenReturn(getTodoTrueData());
		
		//Controllerに投げるリクエストを作成
		MockHttpServletRequestBuilder getRequest = 
				MockMvcRequestBuilders.post("/todo/finish")
										.param("todoId", "cceae402-c5b1-440f-bae2-7bee19dc17fb")
										.param("todoTitle", "one");
		
		//Controllerにリクエストを投げる（リクエストからfinishメソッドを起動させる。）
		ResultActions results = mockMvc.perform(getRequest);
		
		//結果検証
		results.andDo(print());
		results.andExpect(status().isFound());
		results.andExpect(view().name("redirect:/todo/list"));
		
		//FlashMapのデータを取得(model.addFlashAttributeに設定したmessageオブジェクトのtextを取得する。半ば強引)
		FlashMap flashMap = results.andReturn().getFlashMap();
		Collection<Object> collection = flashMap.values();
		for(Object obj : collection) {
			ResultMessages messages = (ResultMessages) obj;
			ResultMessage message = messages.getList().get(0);
			String text = message.getText();
			assertEquals("Finished successfully!", text);
		}
	}
	
	//serviceメソッドが異常を返した場合
	@Test
	public void testFinishThrowException() throws Exception {
		
		//mockを作成(exceptionを返す)
		ResultMessages messages = ResultMessages.error();
		messages.add(ResultMessage.fromText("[E004]The requested Todo is not found. (id=cceae402-c5b1-440f-bae2-7bee19dc17fb)"));
		
		//mockを作成
		when(todoService.finish(anyString())).thenThrow(new BusinessException(messages));
		
		//Controllerに投げるリクエストを作成
		MockHttpServletRequestBuilder getRequest = 
				MockMvcRequestBuilders.post("/todo/finish")
										.param("todoId", "cceae402-c5b1-440f-bae2-7bee19dc17fb")
										.param("todoTitle", "one");
		
		//Controllerにリクエストを投げる（リクエストからfinishメソッドを起動させる。）
		ResultActions results = mockMvc.perform(getRequest);
		
		//結果検証
		results.andDo(print());
		results.andExpect(status().isOk());
		results.andExpect(view().name("todo/list"));
		
		//model Confirmation
		ModelAndView mav = results.andReturn().getModelAndView();
		
		//結果検証
		ResultMessages actResultMessages = (ResultMessages)mav.getModel().get("resultMessages");
		ResultMessage actResultMessage = actResultMessages.getList().get(0);
		String text = actResultMessage.getText();
		assertEquals("[E004]The requested Todo is not found. (id=cceae402-c5b1-440f-bae2-7bee19dc17fb)", text);
	}
	
	//モック用の戻り値データを作成
	public Todo getTodoTrueData() throws Exception {
		
		Todo todo = new Todo();
		todo.setTodoId("cceae402-c5b1-440f-bae2-7bee19dc17fb");
		todo.setTodoTitle("one");
		todo.setFinished(true);
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String strDate = "2017-10-01 15:39:17.888";
		Date date1 = sdFormat.parse(strDate);
		todo.setCreatedAt(date1);
		
		return todo;
	}

}
