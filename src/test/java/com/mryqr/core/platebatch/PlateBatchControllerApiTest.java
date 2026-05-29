package com.mryqr.core.platebatch;

import com.mryqr.BaseApiTest;
import com.mryqr.common.utils.PagedList;
import com.mryqr.core.platebatch.command.CreatePlateBatchCommand;
import com.mryqr.core.platebatch.command.RenamePlateBatchCommand;
import com.mryqr.core.platebatch.domain.PlateBatch;
import com.mryqr.core.platebatch.domain.event.PlateBatchCreatedEvent;
import com.mryqr.core.platebatch.query.ListMyManagedPlateBatchesQuery;
import com.mryqr.core.platebatch.query.QManagedListPlateBatch;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static com.mryqr.common.event.DomainEventType.PLATE_BATCH_CREATED;
import static com.mryqr.common.exception.ErrorCode.PLATE_BATCH_WITH_NAME_ALREADY_EXISTS;
import static com.mryqr.utils.RandomTestFixture.rPlateBatchName;
import static com.mryqr.utils.RandomTestFixture.rQrName;
import static org.junit.jupiter.api.Assertions.*;

class PlateBatchControllerApiTest extends BaseApiTest {

    @Test
    public void should_create_plate_batch() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);

        PlateBatch plateBatch = plateBatchRepository.byId(plateBatchId);
        assertEquals(response.getAppId(), plateBatch.getAppId());
        assertEquals(10, plateBatch.getAvailableCount());
        assertEquals(10, plateBatch.getTotalCount());
        assertEquals(0, plateBatch.getUsedCount());
    }

    @Test
    public void should_raise_event_when_create_plate_batch() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);

        PlateBatchCreatedEvent event = latestEventFor(plateBatchId, PLATE_BATCH_CREATED, PlateBatchCreatedEvent.class);
        assertEquals(plateBatchId, event.getBatchId());
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(10, tenant.getResourceUsage().getPlateCount());
    }

    @Test
    public void should_fail_create_if_name_already_exists() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreatePlateBatchCommand command = CreatePlateBatchCommand.builder().appId(response.getAppId()).name(rPlateBatchName()).total(10).build();
        PlateBatchApi.createPlateBatch(response.getJwt(), command);

        assertError(() -> PlateBatchApi.createPlateBatchRaw(response.getJwt(), command), PLATE_BATCH_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_rename_plate_batch() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);

        String newName = rPlateBatchName();
        PlateBatchApi.renamePlateBatch(response.getJwt(), plateBatchId, newName);

        PlateBatch plateBatch = plateBatchRepository.byId(plateBatchId);
        assertEquals(newName, plateBatch.getName());
    }

    @Test
    public void should_fail_rename_plate_batch_if_name_already_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String previousPlateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        RenamePlateBatchCommand command = RenamePlateBatchCommand.builder().name(rPlateBatchName()).build();
        PlateBatchApi.renamePlateBatch(response.getJwt(), previousPlateBatchId, command);

        assertError(() -> PlateBatchApi.renamePlateBatchRaw(response.getJwt(), plateBatchId, command), PLATE_BATCH_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_delete_plate_batch() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);

        PlateBatchApi.deletePlateBatch(response.getJwt(), plateBatchId);

        assertFalse(plateRepository.byIdOptional(plateBatchId).isPresent());
    }

    @Test
    public void should_raise_event_when_delete_plate_batch() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        List<String> plateIds = plateRepository.allPlateIdsUnderPlateBatch(plateBatchId);
        String plateId = plateIds.stream().findAny().get();
        assertTrue(plateRepository.byId(plateId).isBatched());

        PlateBatchApi.deletePlateBatch(response.getJwt(), plateBatchId);

        assertFalse(plateRepository.byId(plateId).isBatched());
    }

    @Test
    public void should_list_plate_batches() {
        PreparedAppResponse response = setupApi.registerWithApp();
        IntStream.range(0, 11).forEach(value -> PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10));

        ListMyManagedPlateBatchesQuery query = ListMyManagedPlateBatchesQuery.builder()
                .appId(response.getAppId())
                .pageIndex(1)
                .pageSize(10)
                .build();
        PagedList<QManagedListPlateBatch> pagedList1 = PlateBatchApi.listPlateBatches(response.getJwt(), query);
        assertEquals(10, pagedList1.getData().size());

        ListMyManagedPlateBatchesQuery query2 = ListMyManagedPlateBatchesQuery.builder()
                .appId(response.getAppId())
                .pageIndex(2)
                .pageSize(10)
                .build();
        PagedList<QManagedListPlateBatch> pagedList2 = PlateBatchApi.listPlateBatches(response.getJwt(), query2);
        assertEquals(1, pagedList2.getData().size());
        QManagedListPlateBatch qbatch = pagedList2.getData().get(0);
        PlateBatch batch = plateBatchRepository.byId(qbatch.getId());
        assertEquals(batch.getName(), qbatch.getName());
        assertEquals(batch.getCreatedBy(), qbatch.getCreatedBy());
        assertEquals(batch.getTotalCount(), qbatch.getTotalCount());
        assertEquals(batch.getUsedCount(), qbatch.getUsedCount());
    }

    @Test
    public void should_search_plate_batches() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String name = rPlateBatchName();
        CreatePlateBatchCommand command = CreatePlateBatchCommand.builder().appId(response.getAppId()).name(name).total(10).build();
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), command);
        PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);

        ListMyManagedPlateBatchesQuery query = ListMyManagedPlateBatchesQuery.builder()
                .appId(response.getAppId())
                .pageIndex(1)
                .pageSize(10)
                .search(name.substring(5))
                .build();

        PagedList<QManagedListPlateBatch> pagedList = PlateBatchApi.listPlateBatches(response.getJwt(), query);
        assertEquals(1, pagedList.getData().size());
        assertEquals(plateBatchId, pagedList.getData().get(0).getId());
    }

    @Test
    public void should_fetch_all_plate_ids_for_plate_batch() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);

        List<String> plateIds = PlateBatchApi.allPlateIdsUnderPlateBatch(response.getJwt(), plateBatchId);
        assertEquals(10, plateIds.size());
    }

    @Test
    public void should_fetch_unused_plate_ids_for_plate_batch() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        String plateId = plateRepository.allPlateIdsUnderPlateBatch(plateBatchId).stream().findAny().get();
        CreateQrResponse qrResponse = QrApi.createQrFromPlate(response.getJwt(), rQrName(), response.getDefaultGroupId(), plateId);

        List<String> allPlateIds = PlateBatchApi.allPlateIdsUnderPlateBatch(response.getJwt(), plateBatchId);
        assertEquals(10, allPlateIds.size());

        List<String> unusedPlateIds = PlateBatchApi.unusedPlateIdsUnderPlateBatch(response.getJwt(), plateBatchId);
        assertEquals(9, unusedPlateIds.size());

        assertFalse(unusedPlateIds.contains(qrResponse.getPlateId()));
    }

}