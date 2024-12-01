package com.example.eventmaster;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class NotificationUnitTest {

    @Mock
    FirebaseFirestore mockFirestore;

    @Mock
    CollectionReference mockCollectionReference;

    @Mock
    Query mockQuery;

    @Mock
    Task<QuerySnapshot> mockTask;

    @Mock
    NotificationsAdapter mockAdapter;

    @InjectMocks
    Notifications notifications;

    private List<QueryDocumentSnapshot> mockEventDocs;

    @BeforeEach
    public void setUp() {
        mockEventDocs = new ArrayList<>();
        QueryDocumentSnapshot mockDoc1 = Mockito.mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot mockDoc2 = Mockito.mock(QueryDocumentSnapshot.class);

        when(mockDoc1.getId()).thenReturn("Event1");
        when(mockDoc1.getString("facilityId")).thenReturn("Facility1");

        when(mockDoc2.getId()).thenReturn("Event2");
        when(mockDoc2.getString("facilityId")).thenReturn("Facility2");

        mockEventDocs.add(mockDoc1);
        mockEventDocs.add(mockDoc2);

        notifications.inviteList = new ArrayList<>();
        notifications.rejectedList = new ArrayList<>();
        notifications.notificationsAdapter = mockAdapter;
    }

    @Test
    public void testRetrieveNotifiedEvents() {
        String deviceId = "testDevice";

        // Mock Firestore behavior
        when(mockFirestore.collection("entrants")).thenReturn(mockCollectionReference);
        DocumentReference mockDocumentReference = Mockito.mock(DocumentReference.class);
        when(mockCollectionReference.document(deviceId)).thenReturn(mockDocumentReference);
        CollectionReference mockSubCollectionReference = Mockito.mock(CollectionReference.class);
        when(mockDocumentReference.collection("Invited Events")).thenReturn(mockSubCollectionReference);
        when(mockSubCollectionReference.whereNotEqualTo("notifyDate", null)).thenReturn(mockQuery);
        when(mockQuery.orderBy("notifyDate", Query.Direction.DESCENDING)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockTask);

        QuerySnapshot mockQuerySnapshot = Mockito.mock(QuerySnapshot.class);
        when(mockTask.isSuccessful()).thenReturn(true);
        when(mockTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockQuerySnapshot.getDocuments()).thenAnswer(invocation -> mockEventDocs);

        // Call the method
        notifications.retrieveNotifiedEvents(deviceId);

        // Verify Firestore methods were called
        verify(mockFirestore.collection("entrants").document(deviceId).collection("Invited Events")).get();

        // Check results
        assertEquals(2, notifications.inviteList.size());
        assertEquals("Event1", notifications.inviteList.get(0).getEventName());
        assertEquals("Facility1", notifications.inviteList.get(0).getDeviceID());
    }

    @Test
    public void testRetrieveRejectedEvents() {
        String deviceId = "testDevice";

        // Mock Firestore behavior
        when(mockFirestore.collection("entrants")).thenReturn(mockCollectionReference);
        DocumentReference mockDocumentReference = Mockito.mock(DocumentReference.class);
        when(mockCollectionReference.document(deviceId)).thenReturn(mockDocumentReference);
        CollectionReference mockSubCollectionReference = Mockito.mock(CollectionReference.class);
        when(mockDocumentReference.collection("Rejected Events")).thenReturn(mockSubCollectionReference);
        when(mockSubCollectionReference.whereNotEqualTo("notifyDate", null)).thenReturn(mockQuery);
        when(mockQuery.orderBy("notifyDate", Query.Direction.DESCENDING)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockTask);

        QuerySnapshot mockQuerySnapshot = Mockito.mock(QuerySnapshot.class);
        when(mockTask.isSuccessful()).thenReturn(true);
        when(mockTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockQuerySnapshot.getDocuments()).thenAnswer(invocation -> mockEventDocs);

        // Call the method
        notifications.retrieveRejectedEvents(deviceId);

        // Verify Firestore methods were called
        verify(mockFirestore.collection("entrants").document(deviceId).collection("Rejected Events")).get();

        // Check results
        assertEquals(2, notifications.rejectedList.size());
        assertEquals("Event2", notifications.rejectedList.get(1).getEventName());
        assertEquals("Facility2", notifications.rejectedList.get(1).getDeviceID());
    }

    @Test
    public void testAdapterUpdatesOnEventRetrieval() {
        notifications.eventList = new ArrayList<>();

        // Mock Firestore behavior
        String deviceId = "testDevice";
        when(mockFirestore.collection("entrants")).thenReturn(mockCollectionReference);
        DocumentReference mockDocumentReference = Mockito.mock(DocumentReference.class);
        when(mockCollectionReference.document(deviceId)).thenReturn(mockDocumentReference);
        CollectionReference mockSubCollectionReference = Mockito.mock(CollectionReference.class);
        when(mockDocumentReference.collection("Invited Events")).thenReturn(mockSubCollectionReference);
        when(mockSubCollectionReference.get()).thenReturn(mockTask);

        QuerySnapshot mockQuerySnapshot = Mockito.mock(QuerySnapshot.class);
        when(mockTask.isSuccessful()).thenReturn(true);
        when(mockTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockQuerySnapshot.getDocuments()).thenAnswer(invocation -> mockEventDocs);

        // Call the method
        notifications.retrieveNotifiedEvents(deviceId);

        // Verify adapter notified of changes
        verify(mockAdapter).notifyDataSetChanged();
        assertEquals(2, notifications.eventList.size());
    }
}
