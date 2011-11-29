<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"

  xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
  xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
  xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
  xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
  xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
  xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0"
  xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
  xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
  xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
  xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0"
  xmlns:math="http://www.w3.org/1998/Math/MathML"
  xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
  xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
  xmlns:ooo="http://openoffice.org/2004/office"
  xmlns:ooow="http://openoffice.org/2004/writer"
  xmlns:oooc="http://openoffice.org/2004/calc"
  xmlns:dom="http://www.w3.org/2001/xml-events"
  xmlns:xforms="http://www.w3.org/2002/xforms"
  xmlns:xsd="http://www.w3.org/2001/XMLSchema"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:rpt="http://openoffice.org/2005/report"
  xmlns:of="urn:oasis:names:tc:opendocument:xmlns:of:1.2"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:grddl="http://www.w3.org/2003/g/data-view#"
  xmlns:tableooo="http://openoffice.org/2009/table"
  xmlns:field="urn:openoffice:names:experimental:ooo-ms-interop:xmlns:field:1.0"
  xmlns:formx="urn:openoffice:names:experimental:ooxml-odf-interop:xmlns:form:1.0"
  xmlns:css3t="http://www.w3.org/TR/css3-text/"
  office:version="1.2">

  <xsl:output method="xml"/>
  <xsl:strip-space elements="p span"/> 

  <xsl:template match="/">
    <html>
      <head>
        <style type="text/css">
          body {
            background: black;
            align: center;
          }

          div#content {
            border: thick solid black;
            border-radius: 20px;
            background: white;
            width: 800px;
            padding: 50px;
            margin: 0 auto;
          }

          p { margin-bottom: 0.21cm }

          p.toc1 {
            margin-left:   2cm;
            margin-right:  2cm;
            margin-bottom: 0cm;
          }

          .source-file {
            margin-left:   1cm;
            margin-right:  1cm;
            margin-bottom: 0cm;
            margin-top:    0cm;
            border: solid 2px black;
            padding-left: 10px;
            padding-right: 10px;
            background: #ddd;
            white-space: pre;
            font-family: courier;
          }
          .source-file-start {
            margin-top: 0.21cm;
            padding-top: 10px;
            border-bottom: none;
          }
          .source-file-middle {
            border-top: none;
            border-bottom: none;
          }
          .source-file-end {
            border-top: none;
            margin-bottom: 0.21cm;
            padding-bottom: 10px;
          }

          p.caption { font-weight: bold; }

          p.dictionary-term {
            font-weight: bold;
            margin-bottom: 0.10cm;
          }

          p.dictionary-definition {
            padding-left: 1.0cm;
            margin-top: 0cm;
            margin-bottom: 0.21cm;
          }

          p.title {
            font-size: 24pt;
            color: #49c2f1;
          }
          h1 {
            font-size: 24pt;
            color: #49c2f1;
          }
          h2 {
            margin-bottom: 0.21cm
          }
          h3 {
            margin-bottom: 0.21cm
          }
          A:link { so-language: zxx }

          ul {
            list-style-image: url('vaadin-arrow-16px.png');
          }
          
          .illustration {
            text-align: center;
          }

          .classname {
            font-weight: bold;
          }

          .filename {
            font-family: courier;
          }
        </style>
      </head>
      <body bgcolor="#FFFFFF">
        <div id="content">
          <xsl:apply-templates select="office:document-content/office:body/office:text"/>
        </div>
      </body>
    </html>
  </xsl:template>

  <!-- ===================================================================== -->
  <!-- Title                                                                 -->
  <!-- ===================================================================== -->

  <xsl:template match="text:p[@text:style-name='Title']">
    <p class="title"><xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="text:line-break">
    <br/>
  </xsl:template>

  <xsl:template match="style:style">
    <xsl:value-of select="@style:parent-style-name"/>
  </xsl:template>

  <xsl:template name="find-style-name">
    <xsl:param name="stylename"/>
    
    <xsl:variable name="searchresult">
      <xsl:apply-templates select="/office:document-content/office:automatic-styles/style:style[@style:name=$stylename]"/>
    </xsl:variable>
    
    <xsl:choose>
      <xsl:when test="$searchresult = ''">
        <xsl:value-of select="$stylename"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$searchresult"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ===================================================================== -->
  <!-- Paragraphs                                                            -->
  <!-- ===================================================================== -->

  <xsl:template match="text:p">
    <xsl:variable name="stylename">
      <xsl:call-template name="find-style-name">
        <xsl:with-param name="stylename" select="@text:style-name"/>
      </xsl:call-template>      
    </xsl:variable>

    <xsl:variable name="styleclass">
      <xsl:choose>
        <!-- ODF style name to CSS style class mapping -->
        <xsl:when test="$stylename = 'Text_20_body'">normal</xsl:when>
        <xsl:when test="$stylename = 'Introducing_20_list'">normal</xsl:when>
        <xsl:when test="$stylename = 'Caption'">caption</xsl:when>
        <xsl:when test="$stylename = 'Illustration'">illustration</xsl:when>
        <xsl:when test="$stylename = 'Contents_20_1'">toc1</xsl:when>
        <xsl:when test="$stylename = 'Source_20_File'">source-file</xsl:when>
        <xsl:when test="$stylename = 'Source_20_File_20_Start'">source-file source-file-start</xsl:when>
        <xsl:when test="$stylename = 'Source_20_File_20_Middle'">source-file source-file-middle</xsl:when>
        <xsl:when test="$stylename = 'Source_20_File_20_End'">source-file source-file-end</xsl:when>
        <xsl:when test="$stylename = 'Command_20_Line_20_Start'">source-file source-file-start</xsl:when>
        <xsl:when test="$stylename = 'Command_20_Line_20_Middle'">source-file source-file-middle</xsl:when>
        <xsl:when test="$stylename = 'Command_20_Line_20_End'">source-file source-file-end</xsl:when>
        <xsl:when test="$stylename = 'Dictionary_20_Term'">dictionary-term</xsl:when>
        <xsl:when test="$stylename = 'Dictionary_20_Definition'">dictionary-definition</xsl:when>

        <!-- Elements to discard altogether -->
        <xsl:when test="$stylename = 'Contents_20_2'">discard</xsl:when>
        <xsl:when test="$stylename = 'Contents_20_3'">discard</xsl:when>

        <xsl:otherwise>unknown</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="htmlelement">
      <xsl:choose>
        <xsl:when test="$stylename = 'Source_20_File_20_Start'">div</xsl:when>
        <xsl:when test="$stylename = 'Source_20_File_20_Middle'">div</xsl:when>
        <xsl:when test="$stylename = 'Source_20_File_20_End'">div</xsl:when>
        <xsl:when test="$stylename = 'Command_20_Line_20_Start'">div</xsl:when>
        <xsl:when test="$stylename = 'Command_20_Line_20_Middle'">div</xsl:when>
        <xsl:when test="$stylename = 'Command_20_Line_20_End'">div</xsl:when>
        <xsl:otherwise>p</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:choose>
      <!-- Unknown paragraph styles -->
      <xsl:when test="$styleclass = 'unknown'">
        <p style="color: red">
          <xsl:value-of select="$stylename"/>:
          <xsl:apply-templates/>
        </p>
      </xsl:when>

      <!-- Discarded paragraph styles -->
      <xsl:when test="$styleclass = 'discard'"/>

      <!-- Unstyled paragraph -->
      <xsl:when test="$styleclass = 'normal'">
        <p><xsl:apply-templates/></p>
      </xsl:when>

      <!-- Known paragraph style -->
      <xsl:otherwise>
        <xsl:element name="p">
          <xsl:attribute name="class">
            <xsl:value-of select="$styleclass"/>
          </xsl:attribute>
          <xsl:apply-templates/>

          <xsl:text> </xsl:text>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ===================================================================== -->
  <!-- Headings                                                              -->
  <!-- ===================================================================== -->

  <xsl:template match="text:h[@text:outline-level='1']">
    <h1>
      <xsl:number level="any" count="text:h[@text:outline-level='1']"/>
      <xsl:text> </xsl:text>
      <xsl:apply-templates/></h1>
  </xsl:template>

  <xsl:template match="text:h[@text:outline-level='2']">
    <h2>
      <xsl:number level="any" count="text:h[@text:outline-level='1']"/>
      <xsl:text>.</xsl:text>
      <xsl:number level="any" from="text:h[@text:outline-level='1']" count="text:h[@text:outline-level='2']"/>
      <xsl:text> </xsl:text>
      <xsl:apply-templates/></h2>
  </xsl:template>

  <xsl:template match="text:h[@text:outline-level='3']">
    <h3>
      <xsl:number level="any" count="text:h[@text:outline-level='1']"/>
      <xsl:text>.</xsl:text>
      <xsl:number level="any" from="text:h[@text:outline-level='1']" count="text:h[@text:outline-level='2']"/>
      <xsl:text>.</xsl:text>
      <xsl:number level="any" from="text:h[@text:outline-level='2']" count="text:h[@text:outline-level='3']"/>
      <xsl:text> </xsl:text>
      <xsl:apply-templates/></h3>
  </xsl:template>

  <xsl:template match="text:h[@text:outline-level='4']">
    <h4><xsl:apply-templates/></h4>
  </xsl:template>

  <!-- ===================================================================== -->
  <!-- Lists                                                                 -->
  <!-- ===================================================================== -->

  <xsl:template match="text:list[@text:style-name='Numbering_20_1']">
    <ol>
      <xsl:apply-templates/>
    </ol>
  </xsl:template>

  <xsl:template match="text:list">
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>

  <xsl:template match="text:list-item">
    <li>
      <xsl:apply-templates/>
    </li>
  </xsl:template>

  <xsl:template match="text:p[@text:style-name='List_20_1']">
    <p><xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="text:p[@text:style-name='Numbering_20_1']">
    <p><xsl:apply-templates/></p>
  </xsl:template>

  <!-- ===================================================================== -->
  <!-- Figures                                                               -->
  <!-- ===================================================================== -->

  <xsl:template match="draw:frame[@text:anchor-type='as-char']">
    <xsl:element name="img">
      <!-- Image file name -->
      <xsl:attribute name="src">
        <xsl:choose>
          <xsl:when test="contains(draw:image/@xlink:href,'../')">
            <xsl:value-of select="substring(draw:image/@xlink:href,4)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="draw:image/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>

      <!-- Width -->
      <xsl:attribute name="width">
        <xsl:value-of select="number(substring-before(@svg:width,'cm'))*50"/>
      </xsl:attribute>
    </xsl:element>
  </xsl:template>

  <!-- ===================================================================== -->
  <!-- Source Code                                                           -->
  <!-- ===================================================================== -->

  <xsl:template match="text:s">
    <xsl:value-of select="substring('                                                          ',1,@text:c)"/>
  </xsl:template>

  <!-- ===================================================================== -->
  <!-- Text Styles                                                           -->
  <!-- ===================================================================== -->

  <!-- xsl:template match="text:span">
    <xsl:choose>
      <xsl:when test="text:s">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:otherwise>
        <span><xsl:apply-templates/></span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template -->

  <xsl:template match="text:span">
    <xsl:variable name="stylename">
      <xsl:call-template name="find-style-name">
        <xsl:with-param name="stylename" select="@text:style-name"/>
      </xsl:call-template>      
    </xsl:variable>

    <xsl:variable name="styleclass">
      <xsl:choose>
        <!-- ODF text style name to CSS style class mapping -->
        <xsl:when test="$stylename = 'Class_20_Name'">classname</xsl:when>
        <xsl:when test="$stylename = 'Filename'">filename</xsl:when>
        <xsl:otherwise>unknown</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:element name="span">
      <xsl:attribute name="class">
        <xsl:value-of select="$styleclass"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <!-- ===================================================================== -->
  <!-- Eat elements                                                          -->
  <!-- ===================================================================== -->
  <xsl:template match="office:annotation"/>

  <xsl:template match="text:p[@text:style-name='Header']"/>
</xsl:stylesheet>
