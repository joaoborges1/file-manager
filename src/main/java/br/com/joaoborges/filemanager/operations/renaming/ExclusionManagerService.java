
package br.com.joaoborges.filemanager.operations.renaming;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.thoughtworks.xstream.XStream;

/**
 * Gerencia o carregamento dos trechos de nome que devem ser excluidos da operacao.
 * 
 * @author Joao
 * @version 01/08/2011 16:26:11
 */
@Component
public class ExclusionManagerService implements InitializingBean {
	
	private static final Logger LOGGER = Logger.getLogger(ExclusionManagerService.class);

	private XStream xStream;
	private List<String> exclusions;

	public void afterPropertiesSet () throws Exception {
		try {
			this.xStream = new XStream();
			this.xStream.processAnnotations(ExclusionsBean.class);
		} catch (Exception e) {
			LOGGER.error(e);
			throw new RuntimeException("Nao foi possivel iniciar o XStream.", e);
		}
		
		this.exclusions = new ArrayList<>();
		Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("exclusions.xml");
		while (resources.hasMoreElements()) {
			URL exclusionFile = resources.nextElement();
			LOGGER.debug("Parsing: " + exclusionFile.getFile());
			
			InputStream input = exclusionFile.openStream();
			String xml = IOUtils.toString(input);
			LOGGER.info(xml);
			ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
			
			ExclusionsBean bean = (ExclusionsBean) this.xStream.fromXML(stream);
			if (bean.getExclusions() != null) {
				for (Exclusion e : bean.getExclusions()) {
					this.exclusions.add(e.getValue());
				}
			}
		}
	}
	
	public boolean hasExclusionFor (String str, boolean considerPartString) {
		for (String s : this.exclusions) {
			if (str.equalsIgnoreCase(s) || (considerPartString && str.contains(s.toLowerCase()))) {
				return true;
			}
		}
		
		return false;
	}
	
	public List<String> getExclusions () {
		return exclusions;
	}
	
}
