package lghdnov.msocial;

import org.springframework.boot.SpringApplication;

public class TestMsocialApplication {

	public static void main(String[] args) {
		SpringApplication.from(MsocialApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
