package info.xiaomo.core.encode.util;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectUtil
{
	
	
    /**
     * 所有属性缓存 Class->PropertyDesc数组
     */
    private static final ConcurrentHashMap<Class<?>, PropertyDesc[]> propertyDescCache = new ConcurrentHashMap<Class<?>, PropertyDesc[]>();

    /**
     * 缓存单个属性信息 class.name->PropertyDesc
     */
    private static final ConcurrentHashMap<String, PropertyDesc> propertyDescSingleCache = new ConcurrentHashMap<String, PropertyDesc>();

    /**
     * 缓存单个方法信息class.name.parameter...-> Method
     */
    private static final ConcurrentHashMap<String, Method> methodCache = new ConcurrentHashMap<String, Method>();

    /**
     * 获取指定类的所有属性列表
     * @param clazz
     * @return
     */
    public static PropertyDesc[] getPropertyDescs(Class<?> clazz)
    {
        PropertyDesc[] ps = propertyDescCache.get(clazz);
        if (ps == null)
        {
            PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(clazz);
            ps = new PropertyDesc[pds.length];
            for (int i = 0; i < pds.length; i++)
            {
                PropertyDescriptor pd = pds[i];
                PropertyDesc p = new PropertyDesc();
                
                String name = StringUtils.uncapitalize(pd.getName());
                p.setName(name);
                p.setPropertyType(pd.getPropertyType());
                p.setReadMethod(pd.getReadMethod());
                p.setWriteMethod(pd.getWriteMethod());
                
                ps[i] = p;
            }
            propertyDescCache.putIfAbsent(clazz, ps);
        }
        return ps;
    }

    /**
     * 获取指定类的指定属性信息
     * @param clazz 类
     * @param name 属性名称
     * @return
     */
    public static PropertyDesc getPropertyDesc(Class<?> clazz, String name)
    {
    	//由于PropertyUtils的getPropertyDescriptors方法，会将所有属性的首写字母转换为小写返回，所以此处为保持一致强制将name首字母小写
    	String nameKey = StringUtils.uncapitalize(name);
        String key = clazz.getName() + "." + nameKey;
        PropertyDesc pd = propertyDescSingleCache.get(key);
        if (pd == null)
        {
            PropertyDesc[] ps = getPropertyDescs(clazz);
            for (PropertyDesc tmp : ps)
            {
                if (nameKey.equals(tmp.getName()))
                {
                    pd = tmp;
                }
            }
            if (pd != null)
            {
                propertyDescSingleCache.putIfAbsent(key, pd);
            }
            else
            {
                propertyDescSingleCache.putIfAbsent(key, PropertyDesc.NULL);
            }

        }
        else if (pd == PropertyDesc.NULL)
        {
            pd = null;
        }
        return pd;
    }

    /**
     * 获取指定Class的方法
     * @param clazz 
     * @param methodName 方法名称
     * @param parameterTypes 参数类型里列表，不定参数
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws SecurityException, NoSuchMethodException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(clazz.getName()).append(".").append(methodName);
        if (parameterTypes != null)
        {
            for (Class<?> pcls : parameterTypes)
            {
                sb.append(".").append(pcls.getName());
            }
        }
        String key = sb.toString();
        Method method = methodCache.get(key);
        if (method == null)
        {
            method = clazz.getMethod(methodName, parameterTypes);
            if (method != null)
            {
                methodCache.putIfAbsent(key, method);
            }
        }
        return method;
    }

    public static boolean isClassExtends(Class<?> targetClazz, Class<?> superClazz)
    {
        Class<?> clazz = targetClazz.getSuperclass();
        while (clazz != null)
        {
            if (clazz == superClazz)
            {
                return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

}