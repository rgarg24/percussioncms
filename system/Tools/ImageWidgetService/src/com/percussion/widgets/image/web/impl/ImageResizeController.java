      /*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

      package com.percussion.widgets.image.web.impl;

      import com.percussion.error.PSExceptionUtils;
      import com.percussion.util.PSBaseBean;
      import com.percussion.widgets.image.data.CachedImageMetaData;
      import com.percussion.widgets.image.data.ImageData;
      import com.percussion.widgets.image.services.ImageCacheManager;
      import com.percussion.widgets.image.services.ImageResizeManager;
      import net.sf.json.JSON;
      import net.sf.json.JSONObject;
      import net.sf.json.JSONSerializer;
      import org.apache.commons.lang.Validate;
      import org.apache.logging.log4j.LogManager;
      import org.apache.logging.log4j.Logger;
      import org.springframework.beans.factory.annotation.Autowired;
      import org.springframework.stereotype.Controller;
      import org.springframework.validation.BindingResult;
      import org.springframework.web.bind.annotation.ModelAttribute;
      import org.springframework.web.bind.annotation.PostMapping;
      import org.springframework.web.bind.annotation.RequestMapping;
      import org.springframework.web.servlet.ModelAndView;

      import java.awt.*;
      import java.io.ByteArrayInputStream;
      import java.io.InputStream;

      @Controller
      @RequestMapping("/imageWidget/resizeImage.do")
      @PSBaseBean("imageWidgetResize")
      public class ImageResizeController {

          private static final Logger log = LogManager.getLogger(ImageResizeController.class);
          private String viewName="imageWidgetJSONView";
          private String modelObjectName="results";
          @Autowired
          private ImageCacheManager imageCacheManager;
          @Autowired
          private ImageResizeManager imageResizeManager;

          @PostMapping()
          public ModelAndView handle(@ModelAttribute("results") ResizeImageBean bean, BindingResult result) {
              ModelAndView mav = new ModelAndView("imageWidgetJSONView");
              try {
                  CachedImageMetaData cimd = resizeImage(bean);
                  JSON json = JSONSerializer.toJSON(cimd);
                  mav.addObject(getModelObjectName(), json);
              } catch (Exception ex) {
                  String emsg = "Unexpected exception " + PSExceptionUtils.getMessageForLog(ex);
                  log.error(emsg);
                  log.debug(ex);
                  JSON json = new JSONObject().accumulate("error", emsg);
                  mav.addObject(getModelObjectName(), json);
              }

              return mav;
          }

          protected CachedImageMetaData resizeImage(ResizeImageBean bean)
                  throws Exception {
              Dimension size = null;
              Rectangle cropBox = null;
              int rotate = 0;

              Validate.notEmpty(bean.getImageKey(), "You must supply an image key");

              if ((bean.getWidth() != 0) || (bean.getHeight() != 0)) {
                  size = new Dimension(bean.getWidth(), bean.getHeight());
                  log.debug("new image size is {}" ,size);
              }

              if ((bean.getX() != 0) && (bean.getY() != 0) && (bean.getDeltaX() != 0) && (bean.getDeltaY() != 0)) {
                  cropBox = new Rectangle(bean.getX(), bean.getY(), bean.getDeltaX(), bean.getDeltaY());
                  log.debug("new image crop box is {}" , cropBox);
              }
              if (bean.getRotate() != 0) {
                  rotate = bean.getRotate();
                  log.debug("rotate is {}" , rotate);
              }
              ImageData imageData = this.imageCacheManager.getImage(bean.getImageKey());
              Validate.notNull(imageData, "Image to be resized was not found");
              InputStream is = new ByteArrayInputStream(imageData.getBinary());

              this.imageResizeManager.setExtension(imageData.getExt());
              this.imageResizeManager.setContentType(imageData.getMimeType());
              this.imageResizeManager.setImageFormat(imageData.getExt());

              try {
                  ImageData imageReturnData = this.imageResizeManager.generateImage(is, cropBox, size, rotate);
                  String key = this.imageCacheManager.addImage(imageReturnData);
                  return new CachedImageMetaData(imageReturnData, key);
              } catch(IllegalArgumentException e) {
                  log.error("Can not resize image with this format. Error: {}", PSExceptionUtils.getMessageForLog(e));
                  log.debug(PSExceptionUtils.getDebugMessageForLog(e));
              }

              String key = this.imageCacheManager.addImage(imageData);
              return new CachedImageMetaData(imageData, key);


          }

          public String getViewName() {
              return viewName;
          }

          public void setViewName(String viewName) {
              this.viewName = viewName;
          }

          public String getModelObjectName() {
              return this.modelObjectName;
          }

          public void setModelObjectName(String modelObjectName) {
              this.modelObjectName = modelObjectName;
          }

          public ImageCacheManager getImageCacheManager() {
              return this.imageCacheManager;
          }

          public void setImageCacheManager(ImageCacheManager imageCacheManager) {
              this.imageCacheManager = imageCacheManager;
          }

          public ImageResizeManager getImageResizeManager() {
              return this.imageResizeManager;
          }

          public void setImageResizeManager(ImageResizeManager imageResizeManager) {
              this.imageResizeManager = imageResizeManager;
          }
      }
