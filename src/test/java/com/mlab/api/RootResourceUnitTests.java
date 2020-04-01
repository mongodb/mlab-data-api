package com.mlab.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.mlab.ns.Uri;
import com.mlab.ws.Resource;

public class RootResourceUnitTests {

  private RootResource _rootResource;

  @Before
  public void setUp() {
    _rootResource = mock(RootResource.class);
    when(_rootResource.getName()).thenCallRealMethod();
    when(_rootResource.getChildren()).thenCallRealMethod();
    when(_rootResource.resolve((String) any())).thenCallRealMethod();
    when(_rootResource.resolve((Uri) any())).thenCallRealMethod();
    when(_rootResource.resolveRelative(any())).thenCallRealMethod();
    when(_rootResource.getApiConfig()).thenReturn(TestUtils.getTestApiConfig());
  }

  @Test
  public void testGetName() {
    assertEquals("", _rootResource.getName());
  }

  @Test
  public void testGetChildren() {
    final List<Resource> children = _rootResource.getChildren();
    assertEquals(2, children.size());
    assertEquals("clusters", children.get(0).getName());
    assertEquals("databases", children.get(1).getName());
  }

  @Test
  public void testResolve() {
    final Resource clusters = _rootResource.resolve("clusters");
    assertNotNull(clusters);
    assertTrue(clusters instanceof ClustersResource);

    final Resource clusterA = _rootResource.resolve("clusters/a");
    assertNotNull(clusterA);
    assertEquals("a", clusterA.getName());
    assertTrue(clusterA instanceof ClusterResource);
  }
}
