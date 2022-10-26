// Copyright @ MyScript. All rights reserved.

package com.example.drawingink.UIReferenceImplementation;

import androidx.annotation.Nullable;

public interface IInputControllerListener
{
  boolean onLongPress(final float x, final float y, final @Nullable String contentBlockId);
}
