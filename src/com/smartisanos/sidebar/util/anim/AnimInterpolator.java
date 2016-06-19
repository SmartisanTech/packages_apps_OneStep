package com.smartisanos.sidebar.util.anim;

import android.view.animation.AccelerateInterpolator;

public class AnimInterpolator {
    public final static int QUAD_IN = 1;
    public final static int QUAD_OUT = 2;
    public final static int QUAD_IN_OUT = 3;

    public final static int CIRC_IN = 4;
    public final static int CIRC_OUT = 5;
    public final static int CIRC_IN_OUT = 6;

    public final static int CUBIC_IN = 7;
    public final static int CUBIC_OUT = 8;
    public final static int CUBIC_IN_OUT = 9;

    public final static int QUART_IN = 10;
    public final static int QUART_OUT = 11;
    public final static int QUART_IN_OUT = 12;

    public final static int QUINT_IN = 13;
    public final static int QUINT_OUT = 14;
    public final static int QUINT_IN_OUT = 15;

    private interface ComputeInterpolator {
        public float compute(float t);
    }

    public static class Interpolator extends AccelerateInterpolator {

        private int mType = 0;
        private ComputeInterpolator compute;

        public Interpolator(int type) {
            mType = type;
            switch (mType) {
                case QUAD_IN : {
                    compute = new QUAD_IN();
                    break;
                }
                case QUAD_OUT : {
                    compute = new QUAD_OUT();
                    break;
                }
                case QUAD_IN_OUT : {
                    compute = new QUAD_IN_OUT();
                    break;
                }
                case CIRC_IN : {
                    compute = new CIRC_IN();
                    break;
                }
                case CIRC_OUT : {
                    compute = new CIRC_OUT();
                    break;
                }
                case CIRC_IN_OUT : {
                    compute = new CIRC_IN_OUT();
                    break;
                }
                case CUBIC_IN : {
                    compute = new CUBIC_IN();
                    break;
                }
                case CUBIC_OUT : {
                    compute = new CUBIC_OUT();
                    break;
                }
                case CUBIC_IN_OUT : {
                    compute = new CUBIC_IN_OUT();
                    break;
                }
                case QUART_IN : {
                    compute = new QUART_IN();
                    break;
                }
                case QUART_OUT : {
                    compute = new QUART_OUT();
                    break;
                }
                case QUART_IN_OUT : {
                    compute = new QUART_IN_OUT();
                    break;
                }
                case QUINT_IN : {
                    compute = new QUINT_IN();
                    break;
                }
                case QUINT_OUT : {
                    compute = new QUINT_OUT();
                    break;
                }
                case QUINT_IN_OUT : {
                    compute = new QUINT_IN_OUT();
                    break;
                }
            }
        }

        @Override
        public float getInterpolation(float input) {
            if (compute != null) {
                return compute.compute(input);
            }
            return super.getInterpolation(input);
        }
    }

    private static class QUAD_IN implements ComputeInterpolator {
        public float compute(float t) {
            return t*t;
        }
    }

    private static class QUAD_OUT implements ComputeInterpolator {
        public float compute(float t) {
            return -t*(t-2);
        }
    }

    private static class QUAD_IN_OUT implements ComputeInterpolator {
        public float compute(float t) {
            if ((t*=2) < 1) return 0.5f*t*t;
            return -0.5f * ((--t)*(t-2) - 1);
        }
    }

    private static class CIRC_IN implements ComputeInterpolator {
        public float compute(float t) {
            return (float) -Math.sqrt(1 - t*t) - 1;
        }
    }

    private static class CIRC_OUT implements ComputeInterpolator {
        public float compute(float t) {
            return (float) Math.sqrt(1 - (t-=1)*t);
        }
    }

    private static class CIRC_IN_OUT implements ComputeInterpolator {
        public float compute(float t) {
            if ((t*=2) < 1) return -0.5f * ((float)Math.sqrt(1 - t*t) - 1);
            return 0.5f * ((float)Math.sqrt(1 - (t-=2)*t) + 1);
        }
    }

    private static class CUBIC_IN implements ComputeInterpolator {
        public float compute(float t) {
            return t*t*t;
        }
    }

    private static class CUBIC_OUT implements ComputeInterpolator {
        public float compute(float t) {
            return (t-=1)*t*t + 1;
        }
    }

    private static class CUBIC_IN_OUT implements ComputeInterpolator {
        public float compute(float t) {
            if ((t*=2) < 1) return 0.5f*t*t*t;
            return 0.5f * ((t-=2)*t*t + 2);
        }
    }

    private static class QUART_IN implements ComputeInterpolator {
        public float compute(float t) {
            return t*t*t*t;
        }
    }

    private static class QUART_OUT implements ComputeInterpolator {
        public float compute(float t) {
            return -((t-=1)*t*t*t - 1);
        }
    }

    private static class QUART_IN_OUT implements ComputeInterpolator {
        public final float compute(float t) {
            if ((t*=2) < 1) return 0.5f*t*t*t*t;
            return -0.5f * ((t-=2)*t*t*t - 2);
        }
    }

    private static class QUINT_IN implements ComputeInterpolator {
        public float compute(float t) {
            return t*t*t*t*t;
        }
    }

    private static class QUINT_OUT implements ComputeInterpolator {
        public float compute(float t) {
            return (t-=1)*t*t*t*t + 1;
        }
    }

    private static class QUINT_IN_OUT implements ComputeInterpolator {
        public float compute(float t) {
            if ((t*=2) < 1) return 0.5f*t*t*t*t*t;
            return 0.5f*((t-=2)*t*t*t*t + 2);
        }
    }
}