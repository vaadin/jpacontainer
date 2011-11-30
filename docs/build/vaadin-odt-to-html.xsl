<?xml version="1.0" encoding="utf-8"?>

<!-- ======================================================================= -->
<!-- ODT to HTML conversion                                                  -->
<!-- Requires the use of the Vaadin ODT template with proper style names     -->
<!-- Copyright 2011 Vaadin inc.                                              -->
<!-- All rights reserved                                                     -->
<!-- ======================================================================= -->

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
  office:version="1.2">

  <xsl:output method="xml"/>
  <xsl:strip-space elements="p span"/> 

  <xsl:template match="/">
    <html>
      <head>
        <title>
          <xsl:apply-templates select="office:document-content/office:body/office:text" mode="pagetitle"/>
        </title>

        <!-- Customizable stylesheet -->
        <link href="styles.css" rel="stylesheet" type="text/css" />

        <!-- Generate automatic styles -->
        <style type="text/css">
          <xsl:apply-templates select="office:document-content/office:automatic-styles/style:style" mode="autostyles"/>
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

  <!-- Page title -->
  <xsl:template match="text:p[@text:style-name='Title']" mode="pagetitle">
    <xsl:value-of select="text()"/>
  </xsl:template>
  <xsl:template match="text()" mode="pagetitle"/>

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
        <xsl:when test="$stylename = 'INTRO'">intro</xsl:when>
        <xsl:when test="$stylename = 'Title'">title</xsl:when>
        <xsl:when test="$stylename = 'Subtitle'">subtitle</xsl:when>
        <xsl:when test="$stylename = 'Caption'">caption</xsl:when>
        <xsl:when test="$stylename = 'Illustration'">illustration</xsl:when>
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
        <xsl:when test="$stylename = 'Front_20_Matter'">discard</xsl:when>
        <xsl:when test="$stylename = 'Textformatvorlage'">discard</xsl:when>
        <xsl:when test="$stylename = 'Contents_20_Heading'">discard</xsl:when>
        <xsl:when test="$stylename = 'Contents_20_1'">discard</xsl:when>
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
        <xsl:when test="$stylename = 'Method_20_Name'">methodname</xsl:when>
        <xsl:when test="$stylename = 'Parameter'">parameter</xsl:when>
        <xsl:when test="$stylename = 'Filename'">filename</xsl:when>
        <xsl:when test="$stylename = 'GUI_20_Label'">guilabel</xsl:when>
        <xsl:when test="$stylename = 'GUI_20_Button'">guibutton</xsl:when>
        <xsl:when test="$stylename = 'Code_20_Keyword'">code-keyword</xsl:when>
        <xsl:when test="$stylename = 'Code_20_String'">code-string</xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat('auto-', $stylename)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:element name="span">
      <xsl:attribute name="class">
        <xsl:value-of select="$styleclass"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <!-- Automatic style definitions -->
  <xsl:template match="style:style[@style:family='text']" mode="autostyles">
    <xsl:text>.auto-</xsl:text>
    <xsl:value-of select="@style:name"/>
    <xsl:text> {</xsl:text>

    <xsl:if test="style:text-properties/@fo:font-style">
      <xsl:text>font-style: </xsl:text>
      <xsl:value-of select="style:text-properties/@fo:font-style"/>
      <xsl:text>; </xsl:text>
    </xsl:if>

    <xsl:if test="style:text-properties/@fo:font-weight">
      <xsl:text>font-weight: </xsl:text>
      <xsl:value-of select="style:text-properties/@fo:font-weight"/>
      <xsl:text>; </xsl:text>
    </xsl:if>

    <xsl:if test="style:text-properties/@fo:color">
      <xsl:text>color: </xsl:text>
      <xsl:value-of select="style:text-properties/@fo:color"/>
      <xsl:text>; </xsl:text>
    </xsl:if>
    <xsl:text>} </xsl:text>
  </xsl:template>

  <xsl:template match="*" mode="autostyles"/>

  <!-- ===================================================================== -->
  <!-- Hyperlinks                                                            -->
  <!-- ===================================================================== -->
  <xsl:template match="text:a">
    <xsl:element name="a">
      <xsl:attribute name="href">
        <xsl:value-of select="@xlink:href"/>
      </xsl:attribute>

      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <!-- ===================================================================== -->
  <!-- Crossreferences                                                       -->
  <!-- ===================================================================== -->
  <xsl:template match="text:bookmark-ref">
    <xsl:element name="a">
      <xsl:attribute name="href">
        <xsl:value-of select="concat('#',@text:ref-name)"/>
      </xsl:attribute>

      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="text:bookmark">
    <xsl:element name="a">
      <xsl:attribute name="name">
        <xsl:value-of select="@text:name"/>
      </xsl:attribute>

      <xsl:text> </xsl:text>
    </xsl:element>
  </xsl:template>

  <!-- ===================================================================== -->
  <!-- Eat elements                                                          -->
  <!-- ===================================================================== -->
  <xsl:template match="office:annotation"/>
  <xsl:template match="text:table-of-content-source"/>
  <xsl:template match="text:p[@text:style-name='Header']"/>
</xsl:stylesheet>
