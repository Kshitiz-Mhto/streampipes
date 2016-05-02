package de.fzi.cep.sepa.streampipes.codegeneration.flink;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;

import de.fzi.cep.sepa.model.builder.PrimitivePropertyBuilder;
import de.fzi.cep.sepa.model.builder.SchemaBuilder;
import de.fzi.cep.sepa.model.builder.StreamBuilder;
import de.fzi.cep.sepa.model.impl.EventStream;
import de.fzi.cep.sepa.model.impl.eventproperty.EventProperty;
import de.fzi.cep.sepa.model.impl.eventproperty.EventPropertyPrimitive;
import de.fzi.cep.sepa.model.impl.graph.SepaDescription;
import de.fzi.cep.sepa.model.impl.output.AppendOutputStrategy;
import de.fzi.cep.sepa.model.impl.output.OutputStrategy;
import de.fzi.cep.sepa.model.impl.staticproperty.FreeTextStaticProperty;
import de.fzi.cep.sepa.model.impl.staticproperty.MappingPropertyUnary;
import de.fzi.cep.sepa.model.impl.staticproperty.StaticProperty;
import de.fzi.cep.sepa.model.vocabulary.XSD;
import de.fzi.cep.sepa.streampipes.codegeneration.utils.TV;
import de.fzi.cep.sepa.streampipes.codegeneration.utils.Utils;

public class ControllerGeneratorTest {

	@Test
	public void testGetEventStream() {

		Builder b = MethodSpec.methodBuilder("testMethod");
		ControllerGenerator cb = new ControllerGenerator(null, "", "");
		EventStream eventStream = StreamBuilder.createStream(TV.NAME, TV.DESCRIPTION, TV.PATH_NAME)
				.schema(SchemaBuilder.create().build()).build();

		Builder actual = cb.getEventStream(b, eventStream, 0);
		String expected = "void testMethod() {\n  java.util.List<de.fzi.cep.sepa.model.impl.eventproperty.EventProperty> eventProperties0 = new java.util.ArrayList<de.fzi.cep.sepa.model.impl.eventproperty.EventProperty>();\n"
				+ "  de.fzi.cep.sepa.model.impl.EventStream stream0 = de.fzi.cep.sepa.model.builder.StreamBuilder.createStream(\"TestProject\", \"Example description\", \"sepa/testProject\").schema(de.fzi.cep.sepa.model.builder.SchemaBuilder.create().properties(eventProperties0).build()).build();\n}\n";

		assertEquals(expected, actual.build().toString());
	}

	@Test
	public void testGetEventProperties() {
		Builder b = MethodSpec.methodBuilder("testMethod");
		ControllerGenerator cb = new ControllerGenerator(null, "", "");
		List<EventProperty> eventProperties = new ArrayList<EventProperty>();
		eventProperties.add(PrimitivePropertyBuilder.createPropertyRestriction("http://test.org#mytest").build());

		String actual = cb.getEventProperties(b, eventProperties, 0).build().toString();
		String expected = "void testMethod() {\n  java.util.List<de.fzi.cep.sepa.model.impl.eventproperty.EventProperty> eventProperties0 = new java.util.ArrayList<de.fzi.cep.sepa.model.impl.eventproperty.EventProperty>();\n"
				+ "  de.fzi.cep.sepa.model.impl.eventproperty.EventProperty e0 = de.fzi.cep.sepa.model.builder.PrimitivePropertyBuilder.createPropertyRestriction(\"http://test.org#mytest\").build();\n"
				+ "  eventProperties0.add(e0);\n}\n";

		assertEquals(expected, actual);
	}

	@Test
	public void testAppendGetOutputStrategyWithNoEventProperty() {
		ControllerGenerator cb = new ControllerGenerator(null, "", "");
		Builder b = MethodSpec.methodBuilder("testMethod");

		AppendOutputStrategy appendStrategy = new AppendOutputStrategy();

		String actual = cb.getAppendOutputStrategy(b, appendStrategy, 0).build().toString();
		String expected = "void testMethod() {\n"
				+ "  de.fzi.cep.sepa.model.impl.output.AppendOutputStrategy outputStrategy0 = new de.fzi.cep.sepa.model.impl.output.AppendOutputStrategy();\n"
				+ "  java.util.List<de.fzi.cep.sepa.model.impl.eventproperty.EventProperty> appendProperties = new java.util.ArrayList<de.fzi.cep.sepa.model.impl.eventproperty.EventProperty>();\n"
				+ "  outputStrategy0.setEventProperties(appendProperties);\n" + "}\n";

		assertEquals(expected, actual);
	}

	@Test
	public void testAppendGetOutputStrategyWithEventProperties() {
		ControllerGenerator cb = new ControllerGenerator(null, "", "");
		Builder b = MethodSpec.methodBuilder("testMethod");

		AppendOutputStrategy appendStrategy = new AppendOutputStrategy();
		List<EventProperty> eventProperties = new ArrayList<EventProperty>();
		eventProperties.add(new EventPropertyPrimitive(XSD._long.toString(), "testTime", "",
				de.fzi.cep.sepa.commons.Utils.createURI("http://schema.org/Number")));
		appendStrategy.setEventProperties(eventProperties);

		String actual = cb.getAppendOutputStrategy(b, appendStrategy, 0).build().toString();
		String expected = "void testMethod() {\n"
				+ "  de.fzi.cep.sepa.model.impl.output.AppendOutputStrategy outputStrategy0 = new de.fzi.cep.sepa.model.impl.output.AppendOutputStrategy();\n"
				+ "  java.util.List<de.fzi.cep.sepa.model.impl.eventproperty.EventProperty> appendProperties = new java.util.ArrayList<de.fzi.cep.sepa.model.impl.eventproperty.EventProperty>();\n"
				+ "  appendProperties.add(de.fzi.cep.sepa.model.builder.EpProperties.stringEp(\"testTime\", \"http://schema.org/Number\"));\n"
				+ "  outputStrategy0.setEventProperties(appendProperties);\n" + "}\n";

		assertEquals(expected, actual);
	}

	@Test
	public void testGetOutputStrategies() {
		ControllerGenerator cb = new ControllerGenerator(null, "", "");
		Builder b = MethodSpec.methodBuilder("testMethod");

		List<OutputStrategy> strategies = new ArrayList<OutputStrategy>();

		String actual = cb.getOutputStrategies(b, strategies).build().toString();
		String expected = "void testMethod() {\n"
				+ "  java.util.List<de.fzi.cep.sepa.model.impl.output.OutputStrategy> strategies = new java.util.ArrayList<de.fzi.cep.sepa.model.impl.output.OutputStrategy>();\n"
				+ "}\n";

		assertEquals(expected, actual);
	}

	@Test
	public void testGetSupportedGrounding() {
		ControllerGenerator cb = new ControllerGenerator(null, "", "");
		Builder b = MethodSpec.methodBuilder("testMethod");

		String actual = cb.getSupportedGrounding(b, null).build().toString();
		String expected = "void testMethod() {\n"
				+ "  desc.setSupportedGrounding(de.fzi.cep.sepa.util.StandardTransportFormat.getSupportedGrounding());\n"
				+ "}\n";

		assertEquals(expected, actual);
	}

	@Test
	public void testGetRuntime() {
		SepaDescription sepa = getSepa();
		ControllerGenerator cd = new ControllerGenerator(sepa, TV.NAME, TV.PACKAGE_NAME);

		String actual = cd.getRuntime().build().toString();
		String expected = "@java.lang.Override\n"
				+ "protected de.fzi.cep.sepa.flink.FlinkSepaRuntime<de.fzi.cep.sepa.flink.test.project.TestProjectParameters> getRuntime(de.fzi.cep.sepa.model.impl.graph.SepaInvocation graph) {\n"
				+ "  String mappingFirst = de.fzi.cep.sepa.model.util.SepaUtils.getMappingPropertyName(graph, \"mappingFirst\");\n" + 
				"  String freeText = de.fzi.cep.sepa.model.util.SepaUtils.getFreeTextStaticPropertyValue(graph, \"freeText\");\n"
				+ "  de.fzi.cep.sepa.flink.test.project.TestProjectParameters staticParam = new de.fzi.cep.sepa.flink.test.project.TestProjectParameters(graph, mappingFirst, freeText);\n"
				+ "  return new " + TV.PACKAGE_NAME +"."+ TV.NAME
				+ "Program(staticParam, new de.fzi.cep.sepa.flink.FlinkDeploymentConfig(" + TV.PACKAGE_NAME
				+ ".Config.JAR_FILE, " + TV.PACKAGE_NAME + ".Config.FLINK_HOST, " + TV.PACKAGE_NAME
				+ ".Config.FLINK_PORT));\n"
				+ "}\n";


		assertEquals(expected, actual);
	}

	@Test
	public void testBuild() {
		SepaDescription sepa = new SepaDescription(TV.PATH_NAME, TV.NAME, TV.DESCRIPTION);
		List<EventProperty> eventProperties0 = new ArrayList<EventProperty>();
		eventProperties0.add(PrimitivePropertyBuilder.createPropertyRestriction("http://test.org#mytest").build());
		EventStream es = StreamBuilder.createStream(TV.PATH_NAME, TV.NAME, TV.DESCRIPTION)
				.schema(SchemaBuilder.create().properties(eventProperties0).build()).build();
		sepa.addEventStream(es);

		List<OutputStrategy> strategies = new ArrayList<OutputStrategy>();
		AppendOutputStrategy outputStrategy = new AppendOutputStrategy();
		strategies.add(outputStrategy);
		sepa.setOutputStrategies(strategies);

		String actual = new ControllerGenerator(sepa, TV.NAME, TV.PACKAGE_NAME).build().toString();
		String expected = Utils.readResourceFile("expected_TestProjectController_java");

		assertEquals(expected, actual);
	}

	private SepaDescription getSepa() {
		SepaDescription sepa = new SepaDescription(TV.PATH_NAME, TV.NAME, TV.DESCRIPTION);

		List<EventProperty> eventProperties = new ArrayList<EventProperty>();
		EventProperty e1 = PrimitivePropertyBuilder.createPropertyRestriction("http://test.org#test1").build();
		eventProperties.add(e1);

		EventStream stream1 = StreamBuilder.createStreamRestriction("localhost/sepa/testproject")
				.schema(SchemaBuilder.create().properties(eventProperties).build()).build();
		sepa.addEventStream(stream1);

		List<OutputStrategy> strategies = new ArrayList<OutputStrategy>();
		AppendOutputStrategy outputStrategy = new AppendOutputStrategy();
		List<EventProperty> appendProperties = new ArrayList<EventProperty>();
		appendProperties.add(new EventPropertyPrimitive(XSD._long.toString(), "appendedTime", "",
				de.fzi.cep.sepa.commons.Utils.createURI("http://schema.org/Number")));
		outputStrategy.setEventProperties(appendProperties);
		strategies.add(outputStrategy);
		sepa.setOutputStrategies(strategies);

		List<StaticProperty> staticProperties = new ArrayList<StaticProperty>();
		staticProperties
				.add(new MappingPropertyUnary(URI.create(e1.getElementName()), "mappingFirst", "Mapping First: ", ""));
		staticProperties.add(new FreeTextStaticProperty("freeText", "Free Text: ", ""));

		sepa.setStaticProperties(staticProperties);

		return sepa;
	}

}
