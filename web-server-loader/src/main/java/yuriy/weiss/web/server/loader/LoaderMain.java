package yuriy.weiss.web.server.loader;

import lombok.extern.slf4j.Slf4j;
import yuriy.weiss.common.utils.ThreadUtils;

@Slf4j
public class LoaderMain {

    public static void main( String[] args ) {
        try {
            // new SmallBatchGenerator().sendRequestsWaitThenConsumeResults();
            new ConstantStreamGenerator( false ).generateConstantRequestsStream();
        } catch ( Exception e ) {
            log.error( "Непредвиденная ошибка.", e );
        }
        log.info( "SUCCESS" );
        ThreadUtils.sleep( 60000L );
    }
}
