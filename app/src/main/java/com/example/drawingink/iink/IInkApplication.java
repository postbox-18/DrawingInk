// Copyright @ MyScript. All rights reserved.

package com.example.drawingink.iink;

import android.app.Application;

import com.example.drawingink.certificate.MyCertificate;
import com.myscript.iink.Engine;

public class IInkApplication extends Application
{
  private static Engine engine;

  public static synchronized Engine getEngine()
  {
    if (engine == null)
    {
      engine = Engine.create(MyCertificate.getBytes());
    }

    return engine;
  }

}
