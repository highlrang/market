package com.myproject.myweb.job;

import com.myproject.myweb.domain.user.Role;
import com.myproject.myweb.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

// querydsl이 아닌 JPA용으로
// Reader > JpaCursorItemReader, JpaPagingItemReader이 있고
// Writer > JpaItemWriter이 있음

@Slf4j
@EnableBatchProcessing
@RequiredArgsConstructor
@Configuration
public class UserRoleUpdateJob {

    public static String JOB_NAME = "jpaPagingItemReaderJob";
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private int chunkSize; // writer에서 사용
    @Value("${chunkSize:1000}")
    public void setChunkSize(int chunkSize){
        this.chunkSize = chunkSize;
    }

    // @Bean
    public Job jpaCursorJob(){
        return jobBuilderFactory.get(JOB_NAME)
                .start(jpaCursorStep())
                .build();
    }

    @Bean
    public Step jpaCursorStep() {
        return stepBuilderFactory.get("jpaPagingItemReaderStep")
                .<User, User>chunk(chunkSize)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean // batch 4.3 JpaCursorItemReader
    public JpaPagingItemReader<User> itemReader() {
        return new JpaPagingItemReaderBuilder<User>()
                .name("jpaCursorItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select w" +
                        " from Like l" +
                        " join fetch l.post p" +
                        " join fetch p.writer w" +
                        " where w.role = NORMAL_USER" + // 되는지 확인
                        " group by l.post.writer" +
                        " having count(l.post.writer) >= 100" +
                        " order by count(l.post.writer) desc")
                        // group by는 필요치 않은 정렬 수행함
                        // index 컬럼이면 성능에 영향 끼치지 않지만
                        // 정렬 필요없는 경우라면 order by null을 통해 정렬 효과 제거해야함
                // .parameterValues()
                .build();
    }

    @Bean
    public ItemProcessor<User, User> itemProcessor() {
        return user -> {
            user.roleUpdate(Role.SILVAL_USER);
            return user;
        };
    }

    @Bean
    public JpaItemWriter<User> itemWriter() {
        JpaItemWriter<User> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    /*
    private ItemWriter<User> cursorWriter() { // log custom
        return list -> {
            list.forEach(user -> log.info("user name = " + user.getName()));

        };
    }
     */

}
