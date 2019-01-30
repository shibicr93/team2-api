package spicinemas.api.db;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import spicinemas.api.model.Show;
import spicinemas.api.model.ShowVO;

import java.util.Date;
import java.util.List;

@Repository
@Transactional
public class ShowRepository {
    @Autowired
    private DSLContext dsl;
    public long addShow(Show currentShow) {
        return (long) dsl.insertInto(DSL.table("public.Show"), DSL.field("screenId"), DSL.field("movieId"), DSL.field("showTime"))
                .values(currentShow.getScreenId(), currentShow.getMovieId(), currentShow.getShowTime())
                .returning(DSL.field("id"))
                .fetchOne()
                .get(DSL.field("id"));
    }

    public Show getShow(long showId) {
        return dsl.select(DSL.field("id"), DSL.field("showTime"))
                .from(DSL.table("Show"))
                .where(DSL.field("Show.id").eq(showId))
                .fetchOne()
                .into(Show.class);
    }

    public List<ShowVO> getShows(long movieId, long location, Date showDate) {

        return dsl.select(DSL.field("public.\"Show\".\"id\"").as("id"),
                DSL.field("public.\"Movie\".\"name\"").as("movieName"),
                DSL.field("public.\"Movie\".\"experiences\"").as("experiences"),
                DSL.field("public.\"Screen\".\"name\"").as("screenName"),
                DSL.field("public.\"Show\".\"showTime\"").as("showTime"),
                DSL.field("public.\"Screen\".\"capacity\"").as("capacity"))
                .from(DSL.table("public.\"Show\""))
                .leftOuterJoin(DSL.table("public.\"Screen\""))
                .on(DSL.field("public.\"Show\".\"screenId\"").eq(DSL.field("public.\"Screen\".\"id\"")))

                .leftOuterJoin(DSL.table("public.\"Movie\""))
                .on(DSL.field("public.\"Show\".\"movieId\"").eq(DSL.field("public.\"Movie\".\"id\"")))

                .where("\"movieId\" = " + movieId)
                .and("\"locationId\" = "+ location)
                .and("\"showTime\" >= '"+ showDate +"'::date")
                .and("\"showTime\" < ('"+ showDate +"'::date + '1 day'::interval)")
                .fetchInto(ShowVO.class);
    }

    public List getDistinctMovieIdsByLocation(long locationId) {

        String query = "select public.\"Show\".\"movieId\" from public.\"Show\" join public.\"Screen\" on public.\"Show\".\"screenId\" = public.\"Screen\".id where public.\"Screen\".\"locationId\" ="+locationId;
        List records = dsl.fetch(query).getValues(0);
        return records;

    }
}
