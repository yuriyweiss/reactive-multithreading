package yuriy.weiss.web.server.loader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoaderMain {

    public static void main( String[] args ) {
        try {
            // new SmallBatchGenerator().sendRequestsWaitThenConsumeResults();
            new ConstantStreamGenerator().generateConstantRequestsStream();
        } catch ( Exception e ) {
            log.error( "Непредвиденная ошибка.", e );
        }
        log.info( "SUCCESS" );
    }
}
